package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import no.elhub.tools.autorelease.DistributionManagementOption
import no.elhub.tools.autorelease.config.Configuration
import no.elhub.tools.autorelease.config.Field
import no.elhub.tools.autorelease.io.DistributionManagement
import no.elhub.tools.autorelease.io.MavenPomReader
import no.elhub.tools.autorelease.io.MavenPomWriter
import no.elhub.tools.autorelease.io.MavenPomWriter.appendDistributionManagement
import no.elhub.tools.autorelease.io.MavenPomWriter.writeTo
import no.elhub.tools.autorelease.io.XmlReader
import no.elhub.tools.autorelease.log.Logger
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

class MavenProject(
    override val configFilePath: String = "pom.xml",
    private val clean: Boolean,
    private val skipTests: Boolean,
    private val extraParams: List<String>,
    private val configuration: Configuration,
    private val distributionManagementOption: DistributionManagementOption?,
) : Project {
    override val versionRegex: VersionRegex = VersionRegex.MAVEN

    override fun init(logger: Logger) {
        distributionManagementOption?.let {
            logger.info("Update distributionManagement configuration for maven project")
            setMavenDistributionManagement(it)
        }
    }

    /**
     * Sets the [distributionManagement] configuration in the maven pom.xml [file].
     *
     * If the [file] already contains `distributionManagement` node, it will first be deleted,
     * and then a new one appended with the new [distributionManagement] configuration values.
     */
    @OptIn(ExperimentalSerializationApi::class)
    private fun setMavenDistributionManagement(
        distributionManagementOption: DistributionManagementOption
    ) {
        val buildFile = Path(configFilePath)
        val distributionManagement: DistributionManagement = distributionManagementOption
            .distributionManagementFile
            ?.let { f -> Json.decodeFromStream(f.inputStream()) }
            ?: Json.decodeFromString(distributionManagementOption.distributionManagementString)

        val parent = MavenPomReader.getProjectDistributionManagement(buildFile)?.let {
            it.parentNode.also { p -> p.removeChild(it) }
        } ?: MavenPomReader.getProject(buildFile)
        parent.appendDistributionManagement(distributionManagement)
        parent.writeTo(buildFile)
    }

    override fun publishCommand(): String = buildString {
        append("mvn")
        if (clean) append(" clean")
        append(" deploy")
        if (skipTests) append(" -DskipTests")
        append(" -DfailIfNoTests=false")

        extraParams.forEach { extraParam ->
            append(" $extraParam")
        }

        val mavenSettingsPath = System.getenv()["MAVEN_SETTINGS_PATH"]

        if (mavenSettingsPath != null) append(" --settings '$mavenSettingsPath'")
    }

    override fun setVersion(logger: Logger, nextVersion: Semver?) {
        logger.info("Set next version in ${configFilePath}...")

        val buildFile = Path(configFilePath)
        val nextVersionString = String.format(versionRegex.versionFormat, nextVersion)

        // Recursively set the project version on any submodules as well as the main pom.xml
        setProjectVersion(buildFile, nextVersionString)

        // Set potential extra fields in the configuration
        setExtraFields(nextVersionString)
    }

    /**
     * This method is called recursively to support maven submodules
     */
    private fun setProjectVersion(projectPomFile: Path, nextVersionString: String) {
        with(MavenPomWriter) {
            MavenPomReader.getProjectVersion(projectPomFile)?.let {
                it.nodeValue = nextVersionString
                it.writeTo(projectPomFile)
            }

            val moduleNodes = MavenPomReader.getProjectModules(projectPomFile)
            for (i in 0 until moduleNodes.length) {
                val moduleName = moduleNodes.item(i).textContent
                val modulePomFile = Path(projectPomFile.toFile().absolutePath)
                    .parent.resolve("$moduleName/pom.xml")

                if (modulePomFile.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                    MavenPomReader.getProjectParentVersion(modulePomFile).also {
                        it.nodeValue = nextVersionString
                        it.writeTo(modulePomFile)
                    }
                    setProjectVersion(modulePomFile, nextVersionString)
                } else throw NullPointerException(
                    "Build file for '$moduleName' module does not exist at '$modulePomFile'"
                )
            }
        }
    }

    private fun setExtraFields(nextVersionString: String) {
        configuration.extra.forEach { (path: String, xmlns: String, fields: List<Field>) ->
            val file = Path(path)
            val reader = object : XmlReader(xmlns) {}

            fields.forEach { field ->
                reader.getField(file, field)?.let { f ->
                    field.attributes.forEach { a ->
                        f.attributes.getNamedItem(a.name)?.let {
                            if (a.value != "") it.nodeValue = a.value ?: nextVersionString
                        }
                    }
                    if (field.value != "") f.textContent = field.value ?: nextVersionString
                    f.writeTo(file)
                }
            }
        }
    }
}