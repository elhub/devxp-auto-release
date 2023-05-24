package no.elhub.tools.autorelease.io

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

object NpmPackageJsonReader {

    fun fromFile(file: File): Map<String, JsonElement> {
        return Json.decodeFromString(serializer<MutableMap<String, JsonElement>>(), file.readText())
    }

}
