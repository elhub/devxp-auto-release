package no.elhub.tools.autorelease

import no.elhub.tools.autorelease.log.Logger
import no.elhub.tools.autorelease.project.ProjectType
import no.elhub.tools.autorelease.project.VersionBump
import no.elhub.tools.autorelease.project.VersionFile
import no.elhub.tools.autorelease.project.VersionedRepository
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable
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

    override fun call(): Int {
        val log = Logger(verboseMode)
        log.info("Processing a project of type $project...")
        val repository = VersionedRepository(Paths.get(path).toFile())
        log.info("Current version: ${repository.currentVersion}")
        log.info("Unprocessed messages: ${repository.untaggedMessages.size}")
        val currentVersion = repository.currentVersion
        val increaseVersion = VersionBump.analyze(repository.untaggedMessages)
        log.info("Setting version...")
        val nextVersion = currentVersion.increase(increaseVersion)
        val nextVersionString = if (nextVersion != currentVersion) {
            nextVersion
        } else { // Minor bump and add snapshot
            "${currentVersion.increase(VersionBump.MINOR)}-SNAPSHOT"
        }
        log.info("Next version: $nextVersionString")
        project.versionRegex?.let {
            VersionFile.setVersion(
                Paths.get(project.configFilePath),
                it,
                String.format(project.versionFormat, nextVersionString)
            )
        }
        return if (nextVersion != currentVersion) {
            repository.setTag("v$nextVersionString")
            if (project.publishCommand.isNotEmpty()) {
                log.info("Publish release...")
                val proc = Proc(File(path), Logger(verboseMode))
                val cmd = proc.runCommand(project.publishCommand).also {
                    it.waitFor()
                    log.debug(it.inputStreamAsText())
                    if (it.errorStreamAsText().isNotEmpty()) log.error(it.errorStreamAsText())
                }
                cmd.exitValue()
            } else 0
        } else {
            log.debug("Nothing to do: nextVersion == currentVersion. Exiting...")
            0
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
