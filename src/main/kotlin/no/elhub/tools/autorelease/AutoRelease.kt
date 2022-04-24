package no.elhub.tools.autorelease

import io.github.serpro69.semverkt.release.Increment
import io.github.serpro69.semverkt.release.SemverRelease
import io.github.serpro69.semverkt.release.configuration.PropertiesConfiguration
import no.elhub.tools.autorelease.log.Logger
import no.elhub.tools.autorelease.project.ProjectType
import no.elhub.tools.autorelease.project.ProjectType.ANSIBLE
import no.elhub.tools.autorelease.project.VersionFile
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "auto-release",
    mixinStandardHelpOptions = true,
    description = ["auto-release ."],
    optionListHeading = "@|bold %nOptions|@:%n",
    versionProvider = ManifestVersionProvider::class,
    sortOptions = false
)
class AutoRelease : Callable<Int> {

    @CommandLine.Parameters(
        index = "0",
        description = ["The file path to process (defaults to current location)"],
        defaultValue = "."
    )
    private var path = "."

    @CommandLine.Option(
        names = ["-p", "--project"],
        description = [
            "The type of the project.",
            "Use one of: \${COMPLETION-CANDIDATES}.",
            "Default: \${DEFAULT-VALUE}."
        ]
    )
    private var project: ProjectType = ProjectType.GENERIC

    @CommandLine.Option(
        names = ["-i", "--increment"],
        description = [
            "Version component to bump",
            "Use one of: \${COMPLETION-CANDIDATES}.",
            "Default: \${DEFAULT-VALUE}."
        ]
    )
    private var increment: Increment = Increment.NONE

    @CommandLine.Option(
        names = ["-e", "--extra-param"],
        description = [
            "Extra parameter for the gradle/maven/npm publish task."
        ]
    )
    private var extraParams: Array<String> = arrayOf()

    @Suppress("ArrayPrimitive")
    @CommandLine.Option(
        names = ["-v", "--verbose"],
        description = [
            "Enable verbose output.",
            "Specify multiple -v options to increase verbosity.",
            "For example, `-v -v`, or `-vv`"
        ],
        required = false
    )
    private var verboseMode: Array<Boolean> = emptyArray()

    @CommandLine.Option(
        names = ["--no-publish"],
        description = [
            "Run the publish command after creating a new version",
        ],
        negatable = true,
        required = false,
    )
    private var publish: Boolean = true

    @CommandLine.Option(
        names = ["--dry-run"],
        description = [
            "Compute the next version without doing anything else",
        ],
        required = false,
    )
    private var dryRun: Boolean = false

    override fun call(): Int {
        val log = Logger(verboseMode)
        log.info("Processing a project of type $project...")
        val props = Properties().also {
            it["git.repo.directory"] = Paths.get(path)
        }
        val config = PropertiesConfiguration(props)
        with(SemverRelease(config)) {
            val latestVersion = currentVersion()
            log.info("Current version: $latestVersion")
            val increaseVersion = with(nextIncrement()) {
                if (increment !in listOf(Increment.DEFAULT, Increment.NONE) && this != Increment.NONE) increment else this
            }
            log.info("Setting next $increaseVersion version...")
            val nextVersion = when (increaseVersion) {
                Increment.MAJOR, Increment.MINOR, Increment.PATCH -> release(increaseVersion)
                Increment.PRE_RELEASE -> {
                    latestVersion?.preRelease?.let {
                        release(increaseVersion)
                    } ?: createPreRelease(Increment.DEFAULT)
                }
                Increment.DEFAULT, Increment.NONE -> latestVersion
            }
            log.info("Next version: $nextVersion")
            return if (!dryRun && nextVersion != latestVersion) {
                project.versionRegex?.let {
                    log.info("Set next version in ${project.configFilePath}...")
                    VersionFile.setVersion(
                        Paths.get(project.configFilePath),
                        project,
                        String.format(project.versionFormat, nextVersion)
                    )
                }
                val repo = Git.open(Paths.get(path).toFile())
                if (project == ANSIBLE) repo.commit(project.configFilePath, "Release v$nextVersion")
                repo.setTag("v$nextVersion")
                if (publish && project.publishCommand.isNotEmpty()) {
                    log.info("Publish release...")
                    val proc = Proc(File(path), Logger(verboseMode))
                    val cmd =
                        proc.runCommand("${project.publishCommand} ${extraParams.joinToString(" ")}".trim()).also {
                            it.waitFor(180, TimeUnit.SECONDS)
                            log.info(it.inputStreamAsText())
                        }
                    cmd.exitValue()
                } else 0
            } else {
                log.debug("Nothing to do. Exiting...")
                0
            }
        }
    }

    private fun Git.setTag(tagName: String) {
        Git(repository).use { git ->
            git.tag().setName(tagName).setMessage(tagName).setAnnotated(true).call()
            //git.push().setTransportConfigCallback(SshConfig(SSH_FILE_PATH, null)).setPushTags().call()
        }
    }

    private fun Git.commit(file: String, msg: String) {
        Git(repository).use { git ->
            git.reset() // unstage changes if any
                .setRef("HEAD")
                .setMode(ResetCommand.ResetType.MIXED)
                .call()

            git.add().addFilepattern(file).call()

            git.commit()
                .setMessage(msg) // QUESTION should we have a default msg for commits?
                .setAuthor("auto-release", "auto-release@elhub.cloud") // TODO make author details configurable
                .call()
        }
    }
}

object ManifestVersionProvider : CommandLine.IVersionProvider {

    @Throws(Exception::class)
    override fun getVersion(): Array<String> {
        return arrayOf(CommandLine::class.java.`package`.implementationVersion.toString())
    }

}

@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoRelease()).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
)
