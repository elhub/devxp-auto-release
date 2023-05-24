package no.elhub.tools.autorelease.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import no.elhub.tools.autorelease.io.MavenPomWriter.appendDistributionManagement
import no.elhub.tools.autorelease.io.MavenPomWriter.writeTo

class MavenPomWriterTest : DescribeSpec({

    describe("A valid maven pom.xml file") {

        val testFile = Paths.get("build/resources/test/pom-wo-dm.xml")

        it("should append project distributionManagement configuration") {
            val project = MavenPomReader.getProject(testFile)
            project.appendDistributionManagement(
                DistributionManagement(
                    Repository(false, "releases", "release repo", "https://example.com/release"),
                    Repository(true, "snapshots", "snapshot repo", "https://example.com/snapshots"),
                )
            )
            val newPom = with(Paths.get("build/resources/test/new-pom-with-dm.xml")) {
                deleteIfExists()
                createFile()
            }
            project.writeTo(newPom)
            MavenPomReader.getProjectDistributionManagement(newPom) shouldNotBe null
        }
    }
})
