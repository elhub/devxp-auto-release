import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.DeployEnvironment.Environment.TEST_11
import no.elhub.devxp.build.configuration.pipeline.constants.DeployEnvironment.Environment.TEST_13
import no.elhub.devxp.build.configuration.pipeline.constants.Group.CORE
import no.elhub.devxp.build.configuration.pipeline.constants.JavaVersion.VERSION_1_8
import no.elhub.devxp.build.configuration.pipeline.jobs.deploy
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleIntegrationTest
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-build-configuration") {

    pipeline {
        sequential {
            val artifacts = gradleVerify()
            gradleAutoRelease(artifacts = listOf(artifacts))
        }
    }
}
