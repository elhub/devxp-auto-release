package no.elhub.tools.autorelease.project

import java.util.Locale

@Suppress("unused")
enum class ProjectType {
    ANSIBLE,
    GENERIC,
    GRADLE,
    MAKE,
    MAVEN,
    NPM,
    NUGET;

    override fun toString() = this.name.lowercase(Locale.getDefault())
}
