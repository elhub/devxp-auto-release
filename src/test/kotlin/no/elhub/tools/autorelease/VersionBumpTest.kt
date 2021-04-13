package no.elhub.tools.autorelease

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class VersionBumpTest : DescribeSpec({

    describe("The VersionBump analyzer") {

        it("should return PATCH if the patch pattern is in the message list") {
            VersionBump.analyze(
                listOf(
                    "Hello World",
                    "This is a [patch]",
                    "Should it?"
                )
            ) shouldBe VersionBump.PATCH
        }

        it("should return MINOR if the minor pattern is in the message list") {
            VersionBump.analyze(
                listOf(
                    "Hello World",
                    "This is a [minor] release",
                    "Should it?"
                )
            ) shouldBe VersionBump.MINOR
        }

        it("should return MAJOR if the major pattern is in the message list") {
            VersionBump.analyze(
                listOf(
                    "Hello World",
                    "This is a [major] release",
                    "Should it?"
                )
            ) shouldBe VersionBump.MAJOR
        }

        it("should return the biggest version bump if there are multiple ones in the list") {
            VersionBump.analyze(
                listOf(
                    "Hello World [minor]",
                    "This is a [patch]",
                    "Should it?"
                )
            ) shouldBe VersionBump.MINOR
        }

        it("should return the biggest version bump irrespective of the order") {
            VersionBump.analyze(
                listOf(
                    "Hello World [minor]",
                    "This is a [patch]",
                    "[major] Should it?"
                )
            ) shouldBe VersionBump.MAJOR
        }

    }

})
