package no.elhub.tools.autorelease

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class VersionedRepositoryTest : DescribeSpec({
    val tempFolderPath = File("build/test-git/")

    beforeSpec {
        FileUtils.forceMkdir(tempFolderPath)
        val git = Git.init().setDirectory(tempFolderPath).call()
        File("$tempFolderPath/temp1.txt").createNewFile()
        git.add().addFilepattern(".").call()
        git.commit().setMessage( "Initial Commit" ).call()
        File("$tempFolderPath/temp2.txt").createNewFile()
        git.add().addFilepattern(".").call()
        git.commit().setMessage( "Second Commit" ).call()
        git.tag().setName("v1.2.3").setMessage("Testing v1.2.3").setForceUpdate(true).call();
        File("$tempFolderPath/temp3.txt").createNewFile()
        git.add().addFilepattern(".").call()
        git.commit().setMessage( "Third Commit" ).call()
    }

    describe("The git test repository") {
        val repository = VersionedRepository(tempFolderPath)

        it("should return 1.2.3 as the current version") {
            repository.currentVersion shouldBe Version("1.2.3")
        }

        it("should contain one untagged messages") {
            repository.untaggedMessages.size shouldBe 1
        }

        it("should have a single line commit with the subject Third Commit") {
            repository.untaggedMessages[0] shouldBe "Third Commit"
        }

    }

    afterSpec {
        // Clean up the directory
        FileUtils.deleteDirectory(tempFolderPath)
    }

})
