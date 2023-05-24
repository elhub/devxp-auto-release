package no.elhub.tools.autorelease.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import no.elhub.tools.autorelease.config.Field
import no.elhub.tools.autorelease.io.MavenPomWriter.writeTo

class MavenPomReaderTest : DescribeSpec({

    describe("A valid maven pom.xml file") {

        context("single-module maven project") {
            val testFile = Paths.get("build/resources/test/pom.xml")

            it("should get the xml node with the project version") {
                MavenPomReader.getProjectVersion(testFile)?.textContent shouldBe "0.1.0-SNAPSHOT"
            }

            it("should get distributionManagement tag") {
                val dm = MavenPomReader.getProjectDistributionManagement(testFile.parent.resolve("pom-with-dm.xml"))
                dm shouldNotBe null
            }

            it("should remove distributionManagement tag") {
                val dm = MavenPomReader.getProjectDistributionManagement(testFile.parent.resolve("pom-with-dm.xml"))
                val project = dm?.parentNode
                project?.removeChild(dm)
                with(Path("build/resources/test/no-dm-pom.xml")) {
                    deleteIfExists()
                    createFile()
                    project?.writeTo(this)
                    MavenPomReader.getProjectDistributionManagement(this) shouldBe null
                }
            }

            it("should get a custom Field") {
                val properties = Field("properties", null)
                val test = Field("test", null, properties)
                val testNode = MavenPomReader.getField(testFile, test)
                testNode?.nodeName shouldBe "test"
                testNode?.textContent shouldBe "foo"
            }
        }

        context("parent build file of a multi-module maven project") {
            val testFile = Paths.get("build/resources/test/multi-module-maven/pom.xml")

            it("should get the xml node with the project version") {
                MavenPomReader.getProjectVersion(testFile)?.textContent shouldBe "0.1.0-SNAPSHOT"
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
            val otherTestFile = Paths.get("build/resources/test/multi-module-maven/moduleA/moduleAA/pom.xml")

            it("should get the version of the parent pom") {
                MavenPomReader.getProjectParentVersion(testFile).textContent shouldBe "0.1.0-SNAPSHOT"
                MavenPomReader.getProjectParentVersion(otherTestFile).textContent shouldBe "0.1.0-SNAPSHOT"
            }

            it("should get a custom Field") {
                val properties = Field("properties", null)
                val test = Field("test", null, properties)
                val foo = Field("foo", null, test)
                val fooNode = MavenPomReader.getField(testFile, foo)
                fooNode?.nodeName shouldBe "foo"
                fooNode?.textContent shouldBe "bar"
            }
        }
    }
})
