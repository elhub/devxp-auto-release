package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class AnsibleProject(
    override val configFilePath: String = "galaxy.yml"
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.ANSIBLE

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

    override fun publishCommand(): String = "" // no publish command
}
