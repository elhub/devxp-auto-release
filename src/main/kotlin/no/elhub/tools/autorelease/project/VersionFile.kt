package no.elhub.tools.autorelease.project

import io.github.serpro69.semverkt.spec.Semver
import java.nio.file.Files
import java.nio.file.Files.createTempFile
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import no.elhub.tools.autorelease.config.Configuration
import no.elhub.tools.autorelease.io.DistributionManagement
import no.elhub.tools.autorelease.io.MavenPomReader.getField
import no.elhub.tools.autorelease.io.MavenPomReader.getProject
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectDistributionManagement
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectModules
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectParentVersion
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectVersion
import no.elhub.tools.autorelease.io.MavenPomWriter
import no.elhub.tools.autorelease.io.MavenPomWriter.appendDistributionManagement
import no.elhub.tools.autorelease.io.MavenPomWriter.writeTo
import no.elhub.tools.autorelease.io.NpmPackageJsonWriter
import no.elhub.tools.autorelease.project.ProjectType.ANSIBLE
import no.elhub.tools.autorelease.project.ProjectType.GRADLE
import no.elhub.tools.autorelease.project.ProjectType.MAVEN
import no.elhub.tools.autorelease.project.ProjectType.NPM

object VersionFile {

    /**
     * Sets the [distributionManagement] configuration in the maven pom.xml [file].
     *
     * If the [file] already contains `distributionManagement` node, it will first be deleted,
     * and then a new one appended with the new [distributionManagement] configuration values.
     */
    fun setMavenDistributionManagement(distributionManagement: DistributionManagement, file: Path) {
        val parent = getProjectDistributionManagement(file)?.let {
            it.parentNode.also { p -> p.removeChild(it) }
        } ?: getProject(file)
        parent.appendDistributionManagement(distributionManagement)
        parent.writeTo(file)
    }

    /**
     * Sets the [newVersion] in the [file] for a specified [projectType].
     */
    fun setVersion(file: Path, projectType: ProjectType, newVersion: String) {
        when (projectType) {
            ANSIBLE, GRADLE -> {
                val tempFile = createTempFile(Paths.get("."), null, null)
                val lines = file.readLines().map { line ->
                    when (val versionPattern = projectType.versionRegex) {
                        null -> line
                        else -> if (versionPattern.matches(line)) newVersion else line
                    }
                }
                tempFile.writeLines(lines)
                Files.delete(file)
                Files.move(tempFile, file)
            }
            MAVEN -> setMavenVersion(file, newVersion)
            NPM -> NpmPackageJsonWriter.updateVersion(newVersion, file.toFile())
            else -> { // noop
            }
        }
    }

    fun setExtraFields(projectType: ProjectType, config: Configuration, newVersion: String?) {
        config.extraFields?.let { e ->
            val file = Paths.get(e.file)
            when (projectType) {
                MAVEN -> e.fields.forEach { field ->
                    getField(file, field)?.let {
                        it.textContent = field.value ?: newVersion
                        it.writeTo(file)
                    }
                }
                else -> { // noop
                }
            }
        }
    }

    private fun setMavenVersion(file: Path, newVersion: String) {
        with(MavenPomWriter) {
            getProjectVersion(file)?.let {
                it.nodeValue = newVersion
                it.writeTo(file)
            }

            val moduleNodes = getProjectModules(file)
            for (i in 0 until moduleNodes.length) {
                val moduleName = moduleNodes.item(i).textContent
                val modulePomFile = Paths.get(file.toFile().absolutePath).parent.resolve("$moduleName/pom.xml")
                if (modulePomFile.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                    getProjectParentVersion(modulePomFile).also {
                        it.nodeValue = newVersion
                        it.writeTo(modulePomFile)
                    }
                    setMavenVersion(modulePomFile, newVersion)
                } else throw NullPointerException(
                    "Build file for '$moduleName' module does not exist at '$modulePomFile'"
                )
            }
        }
    }
}
