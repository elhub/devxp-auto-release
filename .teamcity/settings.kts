import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.dsl.elhubProject
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-release") {
    val artifactoryRepository = ArtifactoryRepository.ELHUB_BIN_RELEASE_LOCAL

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
                enablePublishMetrics = true
            }
            gradleAutoRelease(artifacts = listOf(artifacts)) {
                repository = artifactoryRepository
            }
        }
    }
}
