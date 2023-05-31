package no.elhub.tools.autorelease.config

object DefaultConfiguration : Configuration {
    override val startingVersion = "0.1.0"
    override val tagPrefix = "v"
    override val snapshotSuffix = "SNAPSHOT"
    override val prereleaseSuffix = "RC"
    override val majorPattern = "[major]"
    override val minorPattern = "[minor]"
    override val patchPattern = "[patch]"
    override val prereleasePattern = "[rc]"
    override val extra: List<Extra> = emptyList()
}
