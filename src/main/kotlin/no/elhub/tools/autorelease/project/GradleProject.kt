package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class GradleProject(
    override val configFilePath: String = "gradle.properties",
    private val clean: Boolean,
    private val skipTests: Boolean,
    private val extraParams: List<String>,
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.GRADLE

    override fun init(logger: Logger) { /* no-op */
    }

    override fun setVersion(logger: Logger, nextVersion: Semver?) {
        logger.info("Set next version in ${configFilePath}...")

        val buildFile = Path(configFilePath)
        val nextVersionString = String.format(versionRegex.versionFormat, nextVersion)
        val tempFile = createTempFile(Path("."), null, null)

        val lines = buildFile.readLines().map { line ->
            if (versionRegex.regex.matches(line)) nextVersionString else line
        }

        tempFile.writeLines(lines)
        Files.delete(buildFile)
        Files.move(tempFile, buildFile)
    }

    override fun publishCommand(): String {
        val commandList = buildList {
            add("./gradle")

            if (clean) {
                add("clean")
            }

            add("assemble")
            add("publish")

            if (skipTests) {
                add("-x test")
            }

            extraParams.forEach { extraParam ->
                add(extraParam)
            }
        }

        return commandList.joinToString(" ")
    }
}
