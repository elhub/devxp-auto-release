package no.elhub.tools.autorelease

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class StandardPatternTest : DescribeSpec({

    describe("The gradle version pattern") {

        it("should match a standard version") {
            StandardPattern.gradleVersion.matches("version=1.2.3") shouldBe true
        }

        it("should match a snapshot version") {
            StandardPattern.gradleVersion.matches("version=0.1.0-SNAPSHOT") shouldBe true
        }

        it("should handle spaces in the version") {
            StandardPattern.gradleVersion.matches("version = 1.2.3") shouldBe true
        }

        it("should handle spaces before the version") {
            StandardPattern.gradleVersion.matches("    version= 1.2.3") shouldBe true
        }

        it("should handle spaces after the version") {
            StandardPattern.gradleVersion.matches("version=1.2.3   ") shouldBe true
        }

    }

    describe("The maven version pattern") {

        it("should match a standard version") {
            StandardPattern.mavenVersion.matches("<version>1.2.3</version>") shouldBe true
        }

        it("should match a snapshot version") {
            StandardPattern.mavenVersion.matches("<version>0.1.0-SNAPSHOT</version>") shouldBe true
        }

        it("should handle spaces in the version") {
            StandardPattern.mavenVersion.matches("<version>  1.2.3 </version>") shouldBe true
        }

        it("should handle spaces in front of the version") {
            StandardPattern.mavenVersion.matches("   <version>1.2.3 </version>") shouldBe true
        }

        it("should handle spaces after the version") {
            StandardPattern.mavenVersion.matches("<version>1.2.3</version>  ") shouldBe true
        }

    }

})
