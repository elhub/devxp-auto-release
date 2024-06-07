import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-release") {
    pipeline(withReleaseVersion = false) {
        sequential {
            val verifyArtifacts = gradleVerify()
            gradleAutoRelease(artifacts = listOf(verifyArtifacts))
        }
    }
}
