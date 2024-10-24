package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class NuGetProject(
    override val configFilePath: String = ""
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.ANSIBLE
    var version: Semver? = null

    override fun init(logger: Logger) { /* no-op */ }


    override fun setVersion(logger: Logger, nextVersion: Semver?) {
        version = nextVersion
    }

    override fun publishCommand(): String = """
        files=${'$'}(ls build_artifacts/packages)

        for file in ${'$'}files
        do
            curl -X PUT -T build_artifacts/packages/${'$'}file https://jfrog.elhub.cloud:443/artifactory/elhub-nuget-dev-local/ediel/$version/ -u ${'$'}ORG_GRADLE_PROJECT_artifactoryUsername:${'$'}ORG_GRADLE_PROJECT_artifactoryPassword
        done
    """.trimIndent()
}
