package no.elhub.tools.autorelease

import no.elhub.tools.autorelease.project.ProjectType
import no.elhub.tools.autorelease.project.VersionBump
import no.elhub.tools.autorelease.project.VersionFile
import no.elhub.tools.autorelease.project.VersionedRepository
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

    override fun call(): Int {
        println("Processing a project of type $project...")
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
        project.versionRegex?.let {
            VersionFile.setVersion(
                Paths.get(project.configFilePath),
                it,
                String.format(project.versionFormat, nextVersionString)
            )
        }
        if (nextVersion != currentVersion) {
            repository.setTag("v$nextVersionString")
            if (project.publishCommand.isNotEmpty()) {
                println("Publish release...")
                Runtime.getRuntime().exec(project.publishCommand)
            }
        }
        return 0
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(CommandLine(AutoRelease()).execute(*args))
