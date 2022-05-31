package no.elhub.tools.autorelease.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class NpmPackageJsonWriterTest : DescribeSpec({
    describe("A package.json file with a version") {
        val testFile = Paths.get("build/resources/test/package-test.json")

        it("should overwrite the version") {
            NpmPackageJsonWriter.updateVersion("1.2.3", testFile.toFile())
            NpmPackageJsonReader.fromFile(testFile.toFile())["version"] shouldBe "1.2.3".toJsonElement()
        }
        it("should overwrite publishConfig") {
            val publishConfig = mapOf("registry" to "test_repo")
            NpmPackageJsonWriter.updatePublishConfig(publishConfig, testFile.toFile())
            val reg = NpmPackageJsonReader.fromFile(testFile.toFile())["publishConfig"]
            reg shouldBe publishConfig.toJsonElement()
        }
        it("should preserve original non-modified configuration") {
            NpmPackageJsonWriter.updateVersion("1.2.3", testFile.toFile())
            NpmPackageJsonWriter.updatePublishConfig(mapOf("registry" to "test_repo"), testFile.toFile())
            val mod = NpmPackageJsonReader.fromFile(testFile.toFile())
            val orig = NpmPackageJsonReader.fromFile(Paths.get("src/test/resources/package-test.json").toFile())
            mod.filter { it.key !in listOf("version", "publishConfig") }.forEach {
                it.value shouldBe orig[it.key]
            }
        }
    }
})
