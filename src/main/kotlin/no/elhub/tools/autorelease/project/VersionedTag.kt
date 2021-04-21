package no.elhub.tools.autorelease.project

import no.elhub.tools.autorelease.config.Configuration
import org.eclipse.jgit.lib.ObjectId

data class VersionedTag(val tag: String, val commitId: ObjectId) {

    val version: Version
        get() {
            return Version(tag.removePrefix("refs/tags/${Configuration.tagPrefix}"))
        }

}
