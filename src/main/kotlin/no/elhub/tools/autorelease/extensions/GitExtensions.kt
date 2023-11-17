package no.elhub.tools.autorelease.extensions

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand

fun Git.setTag(tagName: String) {
    Git(repository).use { git ->
        git.tag().setName(tagName).setMessage(tagName).setAnnotated(true).call()
    }
}

fun Git.commit(file: String, msg: String) {
    Git(repository).use { git ->
        git.reset() // unstage changes if any
            .setRef("HEAD")
            .setMode(ResetCommand.ResetType.MIXED)
            .call()

        git.add().addFilepattern(file).call()

        git.commit()
            .setMessage(msg) // QUESTION should we have a default msg for commits?
            .setAuthor("auto-release", "auto-release@elhub.cloud") // TODO make author details configurable
            .call()
    }
}