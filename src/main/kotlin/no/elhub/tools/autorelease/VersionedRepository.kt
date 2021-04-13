package no.elhub.tools.autorelease

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.JschConfigSessionFactory
import java.io.File
import java.io.IOException
import java.util.*

const val SSH_FILE_PATH = "~/.ssh/id_rsa"

class VersionedRepository(workingDirectory: File) {
    private val builder: FileRepositoryBuilder
    private val repository: Repository
    private val versionedTags: List<VersionedTag>
    val currentVersion: Version
        get() = versionedTags.lastOrNull()?.version ?: Version("0.0.0")
    val untaggedMessages: List<String>

    init {
        try {
            builder = FileRepositoryBuilder().setWorkTree(workingDirectory).findGitDir(workingDirectory)
            if (!builder.gitDir.exists()) {
                throw VersionException("Unable to find Git repository")
            }
            if (builder.gitDir.parentFile.absolutePath != workingDirectory.absolutePath) {
                builder.workTree = builder.gitDir.parentFile
            }
            repository = builder.build()
        } catch (e: IOException) {
            throw VersionException("Unable to find Git repository: ${e.message}", e)
        }
        // Get all the tags
        versionedTags = filterTags(getAllTags()).sortedBy { it.version }
        // Get messages since last version tag
        untaggedMessages = if (versionedTags.isNotEmpty())
            processCommits(versionedTags.last().commitId)
        else
            ArrayList()
    }

    private fun getActualRefObjectId(ref: Ref): ObjectId {
        val repoPeeled: Ref = repository.refDatabase.peel(ref)
        return if (repoPeeled.peeledObjectId != null) {
            repoPeeled.peeledObjectId
        } else {
            ref.objectId
        }
    }

    private fun getAllTags(): List<VersionedTag> {
        Git(repository).use { git ->
            return git.tagList().call().map { VersionedTag(it.name, getActualRefObjectId(it)) }
        }
    }

    private fun filterTags(refs: List<VersionedTag>): List<VersionedTag> {
        return refs.filter { it.tag.matches(Configuration.tagPattern) }
    }

    private fun processCommits(start: ObjectId): List<String> {
        val messages = ArrayList<String>()
        val head = repository.resolve("HEAD")
        Git(repository).use { git ->
            val logs = git.log().addRange(start, head).call()
            logs.forEach {
                messages.add(it.fullMessage)
            }
        }
        return messages
    }

    fun setTag(tagName: String) {
        Git(repository).use { git ->
            git.tag().setName(tagName).setMessage(tagName).setAnnotated(true).call()
            //git.push().setTransportConfigCallback(SshConfig(SSH_FILE_PATH, null)).setPushTags().call()
        }
    }

}
