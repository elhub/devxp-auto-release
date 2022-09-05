import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.SshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.devxp.build.configuration.Assemble
import no.elhub.devxp.build.configuration.AutoRelease
import no.elhub.devxp.build.configuration.CodeReview
import no.elhub.devxp.build.configuration.ProjectType
import no.elhub.devxp.build.configuration.PublishDocs
import no.elhub.devxp.build.configuration.SonarScan
import no.elhub.devxp.build.configuration.UnitTest
import no.elhub.devxp.build.configuration.constants.GlobalTokens

version = "2022.04"

project {

    val projectId = "no.elhub.devxp:devxp-auto-release"
    val projectType = ProjectType.GRADLE
    val artifactoryRepository = "elhub-bin-release-local"

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val buildChain = sequential {

        buildType(
            UnitTest(
                UnitTest.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = projectType,
                    generateAllureReport = false,
                )
            )
        )

        buildType(
            SonarScan(
                SonarScan.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = projectType,
                    sonarId = projectId
                )
            )
        )

        buildType(
            Assemble(
                Assemble.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = projectType
                )
            )
        )

        val githubAuth = SshAgent {
            teamcitySshKey = "teamcity_github_rsa"
            param("secure:passphrase", GlobalTokens.githubSshPassphrase)
        }

        buildType(
            AutoRelease(
                AutoRelease.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = projectType,
                    repository = artifactoryRepository,
                    sshAgent = githubAuth
                )
            )
        )

        buildType(
            PublishDocs(
                PublishDocs.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = projectType,
                    dest = "devxp/devxp-auto-release"
                )
            ) {
                triggers {
                    vcs {
                        branchFilter = "+:<default>"
                        quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
                    }
                }
            }
        )

    }

    buildChain.buildTypes().forEach { buildType(it) }

    buildType(
        CodeReview(
            CodeReview.Config(
                vcsRoot = DslContext.settingsRoot,
                type = projectType,
                sonarId = projectId,
            )
        )
    )

}
