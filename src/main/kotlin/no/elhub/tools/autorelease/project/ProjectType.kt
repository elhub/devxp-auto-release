package no.elhub.tools.autorelease.project

import java.util.*

@Suppress("unused")
enum class ProjectType {
    ANSIBLE,
    GENERIC,
    GRADLE,
    MAVEN,
    NPM;

    override fun toString() = this.name.lowercase(Locale.getDefault())
}
