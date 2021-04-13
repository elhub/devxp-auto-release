package no.elhub.tools.autorelease

object Configuration {
    const val startingVersion = "0.1.0"
    const val tagPrefix = "v"
    val tagPattern = "^refs/tags/$tagPrefix\\d+\\.\\d+\\.\\d+$".toRegex()
    const val snapshotSuffix = "SNAPSHOT"
    const val prereleaseSuffix = "RC"
    const val majorPattern = "[major]"
    const val minorPattern = "[minor]"
    const val patchPattern = "[patch]"
    const val prereleasePattern = "[rc]"
}
