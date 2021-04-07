import jetbrains.buildServer.configs.kotlin.v2019_2.BuildFeatures
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Trigger
import jetbrains.buildServer.configs.kotlin.v2019_2.VcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.SshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.common.build.configuration.Assemble
import no.elhub.common.build.configuration.AutoRelease
import no.elhub.common.build.configuration.ProjectType
import no.elhub.common.build.configuration.SonarScan
import no.elhub.common.build.configuration.UnitTest
import no.elhub.common.build.configuration.constants.GlobalTokens

version = "2020.2"

project {

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val buildChain = sequential {

        buildType(
            UnitTest(
                UnitTest.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = GRADLE
                )
            )
        )

        buildType(
            SonarScan(
                SonarScan.Config(
                    vcsRoot = DslContext.settingsRoot,
                    sonarId = "no.elhub.tools:dev-tools-auto-release",
                    sonarProjectSources = "src/main",
                    sonarProjectTests = "src/test"
                )
            )
        )

        buildType(
            Assemble(
                Assemble.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = GRADLE
                )
            )
        )

        buildType(
            AutoRelease(
                AutoRelease.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = ProjectType.GRADLE
                )
            ) {
                VcsTrigger ()

                features {

                    sshAgent {
                        teamcitySshKey = "teamcity_github_rsa"
                        param("secure:passphrase", GlobalTokens.githubSshPassphrase)
                    }

                }

            })

    }

    buildChain.buildTypes().forEach { buildType(it) }

}
