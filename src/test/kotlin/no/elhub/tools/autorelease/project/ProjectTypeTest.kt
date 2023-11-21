package no.elhub.tools.autorelease.project

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ProjectTypeTest : DescribeSpec({

    describe("The ansible version pattern") {
        it("should match a standard version") {
            VersionRegex.ANSIBLE.regex.matches("version: 1.2.3") shouldBe true
        }

        it("should match a snapshot version") {
            VersionRegex.ANSIBLE.regex.matches("version: 0.1.0-SNAPSHOT") shouldBe true
        }

        it("should handle no-spaces in the version") {
            VersionRegex.ANSIBLE.regex.matches("version:1.2.3") shouldBe false
        }

        it("should handle spaces in the version") {
            VersionRegex.ANSIBLE.regex.matches("version : 1.2.3") shouldBe false
        }

        it("should handle spaces before the version") {
            VersionRegex.ANSIBLE.regex.matches("    version: 1.2.3") shouldBe false
        }

        it("should handle spaces after the version") {
            VersionRegex.ANSIBLE.regex.matches("version: 1.2.3   ") shouldBe true
        }
    }

    describe("The gradle version pattern") {
        it("should match a standard version") {
            VersionRegex.GRADLE.regex.matches("version=1.2.3") shouldBe true
        }

        it("should match a snapshot version") {
            VersionRegex.GRADLE.regex.matches("version=0.1.0-SNAPSHOT") shouldBe true
        }

        it("should handle spaces in the version") {
            VersionRegex.GRADLE.regex.matches("version = 1.2.3") shouldBe true
        }

        it("should handle spaces before the version") {
            VersionRegex.GRADLE.regex.matches("    version= 1.2.3") shouldBe true
        }

        it("should handle spaces after the version") {
            VersionRegex.GRADLE.regex.matches("version=1.2.3   ") shouldBe true
        }
    }

    describe("The maven version pattern") {
        it("should match a standard version") {
            VersionRegex.MAVEN.regex.matches("<version>1.2.3</version>") shouldBe true
        }

        it("should match a snapshot version") {
            VersionRegex.MAVEN.regex.matches("<version>0.1.0-SNAPSHOT</version>") shouldBe true
        }

        it("should handle spaces in the version") {
            VersionRegex.MAVEN.regex.matches("<version>  1.2.3 </version>") shouldBe true
        }

        it("should handle spaces in front of the version") {
            VersionRegex.MAVEN.regex.matches("   <version>1.2.3 </version>") shouldBe true
        }

        it("should handle spaces after the version") {
            VersionRegex.MAVEN.regex.matches("<version>1.2.3</version>  ") shouldBe true
        }

    }

    describe("The npm version pattern") {
        it("should match a standard version with trailing coma") {
            VersionRegex.NPM.regex.matches("\"version\": \"1.2.3\",") shouldBe true
        }

        it("should match a standard version without trailing coma") {
            VersionRegex.NPM.regex.matches("\"version\": \"1.2.3\"") shouldBe true
        }

        it("should match a snapshot version") {
            VersionRegex.NPM.regex.matches("\"version\": \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces in the version") {
            VersionRegex.NPM.regex.matches("\"version\" : \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces before the version") {
            VersionRegex.NPM.regex.matches("      \"version\" : \"1.2.3-SNAPSHOT\"") shouldBe true
        }

        it("should handle spaces after the version") {
            VersionRegex.NPM.regex.matches("\"version\" : \"1.2.3-SNAPSHOT\"      ") shouldBe true
        }
    }
})
