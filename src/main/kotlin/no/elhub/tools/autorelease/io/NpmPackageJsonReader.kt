package no.elhub.tools.autorelease.io

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.io.File

object NpmPackageJsonReader {

    fun fromFile(file: File): Map<String, JsonElement> {
        return Json.decodeFromString(serializer<MutableMap<String, JsonElement>>(), file.readText())
    }

}
