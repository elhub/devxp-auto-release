package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class MakeProject(
    override val configFilePath: String = "project.properties",
    private val clean: Boolean = false,
    private val skipTests: Boolean = false,
    private val extraParams: List<String> = emptyList()
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.MAKEFILE

    override fun init(logger: Logger) { /* no-op */
    }

    override fun setVersion(logger: Logger, nextVersion: Semver?) {
        logger.info("Set next version in ${configFilePath}...")

        val buildFile = Path(configFilePath)

        val nextVersionString = String.format(versionRegex.versionFormat, nextVersion)
        val tempFile = createTempFile(Paths.get("."), null, null)

        val lines = buildFile.readLines().map { line ->
            if (versionRegex.regex.matches(line)) nextVersionString else line
        }

        tempFile.writeLines(lines)
        Files.delete(buildFile)
        Files.move(tempFile, buildFile)
    }

    override fun publishCommand(): String {
        val commandList = buildList {
            add("make")

            if (clean) {
                add("clean")
            }

            if (!skipTests) {
                add("check")
            }

            add("publish")

            extraParams.forEach { extraParam ->
                add(extraParam)
            }

        }

        return commandList.joinToString(" ")
    }

}
