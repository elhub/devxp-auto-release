package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger


interface Project {

    val versionRegex: VersionRegex
    val configFilePath: String

    fun publishCommand(): String

    /**
     * Optional initialization step the different settings can use. This is ran after the repo has been
     * checked out making it possible to configure files etc.
     */
    fun init(logger: Logger)

    /**
     * Updating the version is determined in each project setting.
     */
    fun setVersion(logger: Logger, nextVersion: Semver?)
}
