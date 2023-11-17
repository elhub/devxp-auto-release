package no.elhub.tools.autorelease.extensions

import io.github.serpro69.semverkt.release.Increment
import io.github.serpro69.semverkt.release.SemverRelease
import io.github.serpro69.semverkt.spec.Semver
import no.elhub.tools.autorelease.log.Logger


fun SemverRelease.determineIncrement(
    log: Logger,
    promoteRelease: Boolean,
    autoReleaseIncrement: Increment
) = if (promoteRelease) Increment.NONE else with(nextIncrement()) {
    log.debug("Next increment from cli option: ${autoReleaseIncrement}")
    log.debug("Next increment from git commit: $this")
    if (autoReleaseIncrement !in listOf(Increment.DEFAULT, Increment.NONE)) {
        if (this == Increment.NONE) this else autoReleaseIncrement
    } else this
}

fun SemverRelease.calculateNextVersion(
    log: Logger,
    promoteRelease: Boolean,
    preRelease: Boolean,
    increaseVersion: Increment,
    latestVersion: Semver?
): Semver? {
    return if (promoteRelease) {
        log.info("Promote to release...")
        promoteToRelease()
    } else if (preRelease) {
        log.info("Create pre-release...")
        createPreRelease(increaseVersion)
    } else when (increaseVersion) {
        Increment.MAJOR, Increment.MINOR, Increment.PATCH -> release(increaseVersion)
        Increment.PRE_RELEASE -> {
            latestVersion?.preRelease?.let {
                release(increaseVersion)
            } ?: createPreRelease(Increment.DEFAULT)
        }

        Increment.DEFAULT, Increment.NONE -> latestVersion
    }
}