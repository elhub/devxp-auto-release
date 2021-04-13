package no.elhub.common.build.configuration

import no.elhub.tools.autorelease.Version
import no.elhub.tools.autorelease.VersionedTag
import io.kotest.matchers.shouldBe
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import org.eclipse.jgit.lib.ObjectId
import java.io.*

class VersionedTagTest : DescribeSpec({
    val objectRef = mockk<ObjectId>()

    describe("A versioned tag for refs/tags/v6.1.0-RC.3") {
        val sut = VersionedTag("refs/tags/v6.1.0-RC.3", objectRef)

        it("should return the version 6.1.0-RC.3") {
            sut.version shouldBe Version("6.1.0-RC.3")
        }

    }

    describe("A versioned tag for refs/tags/v1.2.3") {
        val sut = VersionedTag("refs/tags/v1.2.3", objectRef)

        it("should return the version 1.2.3") {
            sut.version shouldBe Version("1.2.3")
        }

    }


})
