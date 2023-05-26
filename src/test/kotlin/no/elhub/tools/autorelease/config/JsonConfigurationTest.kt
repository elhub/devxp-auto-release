package no.elhub.tools.autorelease.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlin.io.path.Path

class JsonConfigurationTest : DescribeSpec({
    describe("JsonConfiguration") {
        it("should override tagPrefix from json configuration file") {
            val config = JsonConfiguration(Path(ClassLoader.getSystemResource("auto-release-tagPrefix.json").path))
            config.startingVersion shouldBe DefaultConfiguration.startingVersion
            config.tagPrefix shouldBe "ver"
            config.snapshotSuffix shouldBe DefaultConfiguration.snapshotSuffix
            config.prereleaseSuffix shouldBe DefaultConfiguration.prereleaseSuffix
            config.majorPattern shouldBe DefaultConfiguration.majorPattern
            config.minorPattern shouldBe DefaultConfiguration.minorPattern
            config.patchPattern shouldBe DefaultConfiguration.patchPattern
            config.prereleasePattern shouldBe DefaultConfiguration.prereleasePattern
        }
    }
})
