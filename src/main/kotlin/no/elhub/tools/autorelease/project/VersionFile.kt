package no.elhub.tools.autorelease.project

import no.elhub.tools.autorelease.io.MavenPomReader.getProjectModules
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectParentVersion
import no.elhub.tools.autorelease.io.MavenPomReader.getProjectVersion
import no.elhub.tools.autorelease.io.MavenPomWriter
import no.elhub.tools.autorelease.project.ProjectType.ANSIBLE
import no.elhub.tools.autorelease.project.ProjectType.GRADLE
import no.elhub.tools.autorelease.project.ProjectType.MAVEN
import no.elhub.tools.autorelease.project.ProjectType.NPM
import java.nio.file.Files
import java.nio.file.Files.createTempFile
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

object VersionFile {

    /**
     * Sets the [newVersion] in the [file] for a specified [projectType].
     */
    @OptIn(ExperimentalPathApi::class)
    fun setVersion(file: Path, projectType: ProjectType, newVersion: String) {
        when (projectType) {
            MAVEN -> setMavenVersion(file, newVersion)
            GRADLE, NPM, ANSIBLE -> {
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
            else -> { // noop
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
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
