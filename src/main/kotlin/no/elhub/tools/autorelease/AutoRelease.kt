package no.elhub.tools.autorelease

import io.github.serpro69.semverkt.release.Increment
import io.github.serpro69.semverkt.release.SemverRelease
import io.github.serpro69.semverkt.release.configuration.PropertiesConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import no.elhub.tools.autorelease.config.Configuration
import no.elhub.tools.autorelease.config.DefaultConfiguration
import no.elhub.tools.autorelease.config.JsonConfiguration
import no.elhub.tools.autorelease.extensions.calculateNextVersion
import no.elhub.tools.autorelease.extensions.commit
import no.elhub.tools.autorelease.extensions.determineIncrement
import no.elhub.tools.autorelease.extensions.setTag
import no.elhub.tools.autorelease.io.DistributionManagement
import no.elhub.tools.autorelease.io.NpmPackageJsonWriter
import no.elhub.tools.autorelease.log.Logger
import no.elhub.tools.autorelease.project.ProjectType
import no.elhub.tools.autorelease.project.ProjectType.*
import no.elhub.tools.autorelease.project.VersionFile
import org.eclipse.jgit.api.Git
import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val processTimeout: Duration = 180.seconds

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
        ],
        required = false
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

    @CommandLine.Option(
        names = ["--promote-release"],
        description = [
            "Promote a pre-release version to a release",
        ],
        required = false,
    )
    private var promoteRelease: Boolean = false

    @CommandLine.Option(
        names = ["--pre-release"],
        description = [
            "Create a new pre-release version",
        ],
        required = false,
    )
    private var preRelease: Boolean = false

    @CommandLine.Option(
        names = ["--npm-publish-registry"],
        description = [
            "Repository for publishing npm artifacts.",
            "Used with NPM project types",
        ],
        required = false
    )
    private var npmPublishRegistry: String? = null

    @CommandLine.Option(
        names = ["-c", "--config-path"],
        description = [
            "Path to auto-release json configuration file",
        ],
        required = false
    )
    private val configPath: Path? = null

    @ArgGroup(
        exclusive = true,
        multiplicity = "0..1",
        heading = "\nMaven distribution management configuration.\nSee README for additional information on the json structure.\n",
    )
    private var distributionManagement: DistributionManagementOption? = null

    private val config: Configuration by lazy {
        configPath?.let { path -> JsonConfiguration(path) } ?: DefaultConfiguration
    }

    private val semverConfig: PropertiesConfiguration by lazy {
        val props = Properties().also {
            it["git.repo.directory"] = Paths.get(path)
            it["git.tag.prefix"] = config.tagPrefix
            // git message configuration
            it["git.message.major"] = config.majorPattern
            it["git.message.minor"] = config.minorPattern
            it["git.message.patch"] = config.patchPattern
            it["git.message.preRelease"] = config.prereleasePattern
            // version configuration
            it["version.initialVersion"] = config.startingVersion
            it["version.preReleaseId"] = config.prereleaseSuffix
            it["version.snapshotSuffix"] = config.snapshotSuffix
        }

        PropertiesConfiguration(props)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun call(): Int {
        val log = Logger(verboseMode)
        log.info("Processing a project of type $project...")

        with(SemverRelease(semverConfig)) {
            val latestVersion = currentVersion()
            log.info("Current version: $latestVersion")

            val increaseVersion = determineIncrement(log, preRelease, increment)
            log.info("Calculated version increment: '$increaseVersion'")

            val nextVersion = calculateNextVersion(log, promoteRelease, preRelease, increaseVersion, latestVersion)
            log.info("Next version: $nextVersion")

            if (dryRun || nextVersion == latestVersion) {
                log.debug("Nothing to do. Exiting...")
                return 0
            }

            project.versionRegex?.let {
                val buildFile = Paths.get(project.configFilePath)
                when (project) {
                    MAVEN -> {
                        distributionManagement?.let {
                            log.info("Update distributionManagement configuration for maven project")
                            val dm: DistributionManagement = it.distributionManagementFile
                                ?.let { f -> Json.decodeFromStream(f.inputStream()) }
                                ?: Json.decodeFromString(it.distributionManagementString)
                            VersionFile.setMavenDistributionManagement(dm, buildFile)
                        }
                    }

                    NPM -> npmPublishRegistry?.let {
                        log.info("Update publishConfig in package.json for npm project")
                        NpmPackageJsonWriter.updatePublishConfig(
                            mapOf("registry" to it),
                            buildFile.toFile()
                        )
                    }

                    else -> {
                        /*noop*/
                    }
                }
                log.info("Set next version in ${project.configFilePath}...")
                VersionFile.setVersion(buildFile, project, String.format(project.versionFormat, nextVersion))
                VersionFile.setExtraFields(project, config, nextVersion.toString())
            }

            return with(Git.open(Paths.get(path).toFile())) {
                if (project == ANSIBLE) {
                    commit(project.configFilePath, "Release v$nextVersion")
                }

                setTag("v$nextVersion")

                if (publish && project.publishCommand.isNotEmpty()) {
                    log.info("Publish release...")
                    val processLauncher = Proc(File(path), Logger(verboseMode))
                    val publishCommand = publishCommand()

                    processLauncher.runCommand(publishCommand).also { process ->
                        process.waitFor(processTimeout.inWholeSeconds, TimeUnit.SECONDS)
                        log.info(process.inputStreamAsText())
                    }.exitValue()
                } else 0
            }
        }
    }

    private fun publishCommand(): String =
        "${project.publishCommand} ${extraParams.joinToString(" ")}".trim()
            .let { cmd ->
                when (project) {
                    MAVEN -> System.getenv()["MAVEN_SETTINGS_PATH"]?.let { "$cmd --settings '$it'" } ?: cmd
                    else -> cmd
                }
            }
}

object DistributionManagementOption {

    @CommandLine.Option(
        names = ["--maven-dm-string"],
        description = [
            "A json string with maven distribution management configuration.",
        ],
        required = true
    )
    lateinit var distributionManagementString: String

    @CommandLine.Option(
        names = ["--maven-dm-file"],
        description = [
            "A json file with maven distribution management configuration.",
        ],
        required = true
    )
    var distributionManagementFile: File? = null
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
