package no.elhub.tools.autorelease.project

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines

@OptIn(ExperimentalPathApi::class)
class VersionFileTest : DescribeSpec({

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

    afterSpec {
        // Clean up the test files
        Files.delete(Paths.get("build/resources/test/gradle.properties"))
        Files.delete(Paths.get("build/resources/test/pom.xml"))
        Files.delete(Paths.get("build/resources/test/package.json"))
    }
})
