package no.elhub.tools.autorelease.project

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ProjectTypeTest : DescribeSpec({

    describe("The ansible version pattern") {
        val project = ProjectType.ANSIBLE

        it("should match a standard version") {
            project.versionRegex?.matches("version: 1.2.3") shouldBe true
        }

        it("should match a snapshot version") {
            project.versionRegex?.matches("version: 0.1.0-SNAPSHOT") shouldBe true
        }

        it("should handle no-spaces in the version") {
            project.versionRegex?.matches("version:1.2.3") shouldBe false
        }

        it("should handle spaces in the version") {
            project.versionRegex?.matches("version : 1.2.3") shouldBe false
        }

        it("should handle spaces before the version") {
            project.versionRegex?.matches("    version: 1.2.3") shouldBe false
        }

        it("should handle spaces after the version") {
            project.versionRegex?.matches("version: 1.2.3   ") shouldBe true
        }
    }

    describe("The gradle version pattern") {
        val project = ProjectType.GRADLE

        it("should match a standard version") {
            project.versionRegex?.matches("version=1.2.3") shouldBe true
        }

        it("should match a snapshot version") {
            project.versionRegex?.matches("version=0.1.0-SNAPSHOT") shouldBe true
        }

        it("should handle spaces in the version") {
            project.versionRegex?.matches("version = 1.2.3") shouldBe true
        }

        it("should handle spaces before the version") {
            project.versionRegex?.matches("    version= 1.2.3") shouldBe true
        }

        it("should handle spaces after the version") {
            project.versionRegex?.matches("version=1.2.3   ") shouldBe true
        }

    }

    describe("The maven version pattern") {
        val project = ProjectType.MAVEN

        it("should match a standard version") {
            project.versionRegex?.matches("<version>1.2.3</version>") shouldBe true
        }

        it("should match a snapshot version") {
            project.versionRegex?.matches("<version>0.1.0-SNAPSHOT</version>") shouldBe true
        }

        it("should handle spaces in the version") {
            project.versionRegex?.matches("<version>  1.2.3 </version>") shouldBe true
        }

        it("should handle spaces in front of the version") {
            project.versionRegex?.matches("   <version>1.2.3 </version>") shouldBe true
        }

        it("should handle spaces after the version") {
            project.versionRegex?.matches("<version>1.2.3</version>  ") shouldBe true
        }

    }

    describe("The npm version pattern") {
        val project = ProjectType.NPM

        it("should match a standard version with trailing coma") {
            project.versionRegex?.matches("\"version\": \"1.2.3\",") shouldBe true
        }

        it("should match a standard version without trailing coma") {
            project.versionRegex?.matches("\"version\": \"1.2.3\"") shouldBe true
        }

        it("should match a snapshot version") {
            project.versionRegex?.matches("\"version\": \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces in the version") {
            project.versionRegex?.matches("\"version\" : \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces before the version") {
            project.versionRegex?.matches("      \"version\" : \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces after the version") {
            project.versionRegex?.matches("\"version\" : \"1.2.3-SNAPSHOT\"      ") shouldBe true
        }
    }
})
