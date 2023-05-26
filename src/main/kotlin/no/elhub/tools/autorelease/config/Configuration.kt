package no.elhub.tools.autorelease.config

import kotlinx.serialization.Serializable

interface Configuration {
    val startingVersion: String
    val tagPrefix: String
    val snapshotSuffix: String
    val prereleaseSuffix: String
    val majorPattern: String
    val minorPattern: String
    val patchPattern: String
    val prereleasePattern: String
}

@Serializable
data class AutoReleaseConfig(
    val startingVersion: String? = null,
    val tagPrefix: String? = null,
    val snapshotSuffix: String? = null,
    val prereleaseSuffix: String? = null,
    val majorPattern: String? = null,
    val minorPattern: String? = null,
    val patchPattern: String? = null,
    val prereleasePattern: String? = null,
)
