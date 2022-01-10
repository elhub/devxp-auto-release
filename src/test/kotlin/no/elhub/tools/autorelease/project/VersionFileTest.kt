package no.elhub.tools.autorelease.project

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import no.elhub.tools.autorelease.extensions.delete
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectParentVersion
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectVersion
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines

@OptIn(ExperimentalPathApi::class)
class VersionFileTest : DescribeSpec({

    afterSpec {
        // Clean up the test files
        Paths.get("build/resources/test/galaxy.yml").delete()
        Paths.get("build/resources/test/gradle.properties").delete()
        Paths.get("build/resources/test/pom.xml").delete()
        Paths.get("build/resources/test/package.json").delete()
        Paths.get("build/resources/test/multi-module-maven").delete()
    }

    describe("A galaxy.yml file to which VersionFile has been applied") {
        val project = ProjectType.ANSIBLE

        VersionFile.setVersion(
            Paths.get("build/resources/test/galaxy.yml"),
            project,
            String.format(project.versionFormat, "1.2.3")
        )

        it("should have its version set to 1.2.3") {
            val testFile = Paths.get("build/resources/test/galaxy.yml")
            val lines = testFile.readLines()
            lines shouldContain "version: 1.2.3"
        }
    }

    describe("A gradle.properties file to which VersionFile has been applied") {
        val project = ProjectType.GRADLE

        VersionFile.setVersion(
            Paths.get("build/resources/test/gradle.properties"),
            project,
            String.format(project.versionFormat, "1.2.3")
        )

        it("should have its version set to 1.2.3") {
            val testFile = Paths.get("build/resources/test/gradle.properties")
            val lines = testFile.readLines()
            lines shouldContain "version=1.2.3"
        }

    }

    describe("A pom.xml file to which VersionFile has been applied") {
        val project = ProjectType.MAVEN

        context("a single-module project") {
            VersionFile.setVersion(
                Paths.get("build/resources/test/pom.xml"),
                project,
                String.format(project.versionFormat, "1.2.3")
            )

            it("should have exactly one <version/> tag with the value set to 1.2.3") {
                val testFile = Paths.get("build/resources/test/pom.xml")
                testFile.readLines().forExactly(1) { it.trim() shouldBe "<version>1.2.3</version>" }
            }
        }

        context("a multi-module project") {
            VersionFile.setVersion(
                Paths.get("build/resources/test/multi-module-maven/pom.xml"),
                project,
                String.format(project.versionFormat, "1.2.3")
            )

            it("parent pom should have exactly one <version/> tag with the value set to 1.2.3") {
                val testFile = Paths.get("build/resources/test/multi-module-maven/pom.xml")
                testFile.readLines().forExactly(1) { it.trim() shouldBe "<version>1.2.3</version>" }
            }

            it("a <parent/> tag in the parent pom should not be affected") {
                val testFile = Paths.get("build/resources/test/multi-module-maven/pom.xml")
                getProjectParentVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
            }

            listOf("moduleA", "moduleB", "moduleC").forEach { sub ->
                it("$sub pom should have exactly one <version/> under <parent/> tag with the value set to 1.2.3") {
                    val testFile = Paths.get("build/resources/test/multi-module-maven/$sub/pom.xml")
                    getProjectParentVersion(testFile).textContent shouldBe "1.2.3"
                }

                if (sub == "moduleA") {
                    listOf("moduleAA", "moduleAB").forEach { subSub ->
                        it("$subSub pom should have exactly one <version/> under <parent/> tag with the value set to 1.2.3") {
                            val testFile = Paths.get("build/resources/test/multi-module-maven/$sub/$subSub/pom.xml")
                            getProjectParentVersion(testFile).textContent shouldBe "1.2.3"
                        }
                    }
                }
            }

            it("unregistered module's pom.xml should not be affected") {
                val testFile = Paths.get("build/resources/test/multi-module-maven/not-a-module/pom.xml")
                assertSoftly {
                    getProjectVersion(testFile)?.textContent shouldBe "0.1.0-SNAPSHOT"
                    getProjectParentVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
                }
            }
        }
    }

    describe("A package.json file to which VersionFile has been applied") {
        val project = ProjectType.NPM

        VersionFile.setVersion(
            Paths.get("build/resources/test/package.json"),
            project,
            String.format(project.versionFormat, "1.2.3")
        )

        it("should be have its version set to 1.2.3") {
            val testFile = Paths.get("build/resources/test/package.json")
            val lines = testFile.readLines()
            lines.any { it.contains("\"version\": \"1.2.3\"") } shouldBe true
        }
    }
})
