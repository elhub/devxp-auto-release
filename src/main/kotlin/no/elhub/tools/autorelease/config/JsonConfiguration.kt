package no.elhub.tools.autorelease.config

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(ExperimentalSerializationApi::class)
class JsonConfiguration(configPath: Path = Path("./auto-release.json")) : Configuration {
    override var startingVersion: String = DefaultConfiguration.startingVersion
        private set
    override var tagPrefix: String = DefaultConfiguration.tagPrefix
        private set
    override var snapshotSuffix: String = DefaultConfiguration.snapshotSuffix
        private set
    override var prereleaseSuffix: String = DefaultConfiguration.prereleaseSuffix
        private set
    override var majorPattern: String = DefaultConfiguration.majorPattern
        private set
    override var minorPattern: String = DefaultConfiguration.minorPattern
        private set
    override var patchPattern: String = DefaultConfiguration.patchPattern
        private set
    override var prereleasePattern: String = DefaultConfiguration.prereleasePattern
        private set

    init {
        val configStream = configPath.inputStream()
        val config: AutoReleaseConfig = Json.decodeFromStream(configStream)
        config.startingVersion?.let { startingVersion = it }
        config.tagPrefix?.let { tagPrefix = it }
        config.snapshotSuffix?.let { snapshotSuffix = it }
        config.prereleaseSuffix?.let { prereleaseSuffix = it }
        config.majorPattern?.let { majorPattern = it }
        config.minorPattern?.let { minorPattern = it }
        config.patchPattern?.let { patchPattern = it }
        config.prereleasePattern?.let { prereleasePattern = it }
    }
}
