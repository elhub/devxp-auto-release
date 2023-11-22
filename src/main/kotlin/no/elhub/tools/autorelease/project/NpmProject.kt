package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.io.NpmPackageJsonWriter
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.Paths
import kotlin.io.path.Path

class NpmProject(
    override val configFilePath: String = "package.json",
    private val extraParams: List<String>,
    private val npmPublishRegistry: String?,
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.NPM

    override fun init(logger: Logger) {
        val buildFile = Paths.get(configFilePath)

        npmPublishRegistry?.let {
            logger.info("Update publishConfig in package.json for npm project")
            NpmPackageJsonWriter.updatePublishConfig(
                mapOf("registry" to it),
                buildFile.toFile()
            )
        }
    }

    override fun setVersion(logger: Logger, nextVersion: Semver?) {
        logger.info("Set next version in ${configFilePath}...")

        val buildPath = Path(configFilePath)
        val nextVersionString = String.format(versionRegex.versionFormat, nextVersion)

        NpmPackageJsonWriter.updateVersion(nextVersionString, buildPath.toFile())
    }

    override fun publishCommand(): String {
        val commandList = buildList {
            add("npm")
            add("run")
            add("release")

            extraParams.forEach { extraParam ->
                add(extraParam)
            }
        }

        return commandList.joinToString(" ")
    }
}
