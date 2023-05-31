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
    val extra: List<Extra>
}

@Serializable
data class Extra(
    val file: String,
    val xmlns: String = "http://maven.apache.org/POM/4.0.0",
    val fields: List<Field> = emptyList(),
)

@Serializable
data class Field(
    val name: String,
    val value: String? = "",
    val parent: Field? = null,
    val attributes: List<Attribute> = emptyList(),
)

@Serializable
data class Attribute(
    val name: String,
    val value: String? = null,
)

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
    val extra: List<Extra> = emptyList()
)
