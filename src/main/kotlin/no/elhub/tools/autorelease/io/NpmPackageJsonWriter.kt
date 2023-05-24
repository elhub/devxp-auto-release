package no.elhub.tools.autorelease.io

import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object NpmPackageJsonWriter {

    private fun update(field: Pair<String, JsonElement>, file: File) {
        val (name, value ) = field
        val json = NpmPackageJsonReader.fromFile(file).toMutableMap()
        json[name] = value
        val string = Json.encodeToString(json)
        file.writeText(string)
    }


    fun updateVersion(version: String, file: File) {
        val field = "version" to version.toJsonElement()
        println(field.second.jsonPrimitive.toString())
        update(field, file)
    }

    fun updatePublishConfig(publishConfig: Map<String, String>, file: File) {
        val field = "publishConfig" to publishConfig.toJsonElement()
        update(field, file)
    }
}

fun Collection<*>.toJsonElement(): JsonElement = JsonArray(mapNotNull { it.toJsonElement() })

fun Map<*, *>.toJsonElement(): JsonElement = JsonObject(
    mapNotNull {
        (it.key as? String ?: return@mapNotNull null) to it.value.toJsonElement()
    }.toMap(),
)

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Map<*, *> -> toJsonElement()
    is Collection<*> -> toJsonElement()
    else -> JsonPrimitive(toString())
}
