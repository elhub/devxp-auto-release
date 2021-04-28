package no.elhub.tools.autorelease.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class MavenPomReaderTest : DescribeSpec({

    describe("A valid maven pom.xml file") {

        context("single-module maven project") {
            val testFile = Paths.get("build/resources/test/pom.xml")

            it("should get the xml node with the project version") {
                MavenPomReader.getProjectVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
            }
        }

        context("parent build file of a multi-module maven project") {
            val testFile = Paths.get("build/resources/test/multi-module-maven/pom.xml")

            it("should get the xml node with the project version") {
                MavenPomReader.getProjectVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
            }

            it("should get the number of nodes of the project modules") {
                MavenPomReader.getProjectModules(testFile).length shouldBe 3
            }

            it("should get the module name") {
                MavenPomReader.getProjectModules(testFile).item(0).textContent shouldBe "moduleA"
            }
        }

        context("child build file of a multi-module maven project") {
            val testFile = Paths.get("build/resources/test/multi-module-maven/moduleA/pom.xml")

            it("should get the version of the parent pom") {
                MavenPomReader.getProjectParentVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
            }
        }
    }
})
