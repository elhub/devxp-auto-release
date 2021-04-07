package no.elhub.tools.autorelease

import picocli.CommandLine
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "auto-release",
    mixinStandardHelpOptions = true,
    description = ["auto-release ."],
    optionListHeading = "@|bold %nOptions|@:%n",
    sortOptions = false
)
class AutoRelease : Callable<Int> {

    @CommandLine.Parameters(
        index = "0",
        description = ["The file path to process (defaults to current location)"]
    )
    var path = "."

    @CommandLine.Option(
        names = ["-p", "--project"],
        description = ["The type of project. Valid options are: generic, gradle, maven"])
    var project = "generic"

    override fun call(): Int {
        val type: ProjectType
        try {
            type = ProjectType.valueOf(project.toUpperCase())
        } catch (e: IllegalArgumentException ) {
            println("Invalid argument: the project of type $project is not supported by this application")
            return 1
        }
        println("Processing a project of type $type...")
        val repository = VersionedRepository(Paths.get(path).toFile())
        println("Current version: ${repository.currentVersion}")
        println("Unprocessed messages: ${repository.untaggedMessages.size}")
        val currentVersion = repository.currentVersion
        val increaseVersion = VersionBump.analyze(repository.untaggedMessages)
        println("Setting version...")
        val nextVersion = currentVersion.increase(increaseVersion)
        val nextVersionString = if (nextVersion != currentVersion) {
            nextVersion
        } else { // Minor bump and add snapshot
            "${currentVersion.increase(VersionBump.MINOR)}-SNAPSHOT"
        }
        println("Next version: $nextVersionString")
        val publishCommand: String
        when (type) {
            ProjectType.GRADLE -> {
                VersionFile.setVersion(
                    Paths.get("gradle.properties"),
                    StandardPattern.gradleVersion,
                    String.format(StandardPattern.gradleFormat, nextVersionString)
                )
                publishCommand = StandardPattern.gradlePublish
            }
            ProjectType.MAVEN -> {
                VersionFile.setVersion(
                    Paths.get("pom.xml"),
                    StandardPattern.mavenVersion,
                    String.format(StandardPattern.mavenFormat, nextVersionString)
                )
                publishCommand = StandardPattern.mavenPublish
            }
            ProjectType.ANSIBLE, ProjectType.GENERIC -> {
                // For ansible and generic projects, we currently don't need to update any files
                // and no publishing is done (for now). As such, the only thing that happens by
                // default is to tag the repository, if the appropriate commit message is pushed
                publishCommand = ""
            }
            else -> {
                println("Currently auto-release does not handle projects of this type.")
                exitProcess(1)
            }
        }
        if (nextVersion != currentVersion) {
            repository.setTag("v$nextVersionString")
            if (publishCommand.isNotEmpty()) {
                println("Publish release...")
                Runtime.getRuntime().exec(publishCommand)
            }
        }
        return 0
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(CommandLine(AutoRelease()).execute(*args))
