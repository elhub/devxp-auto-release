package no.elhub.tools.autorelease.project

import no.elhub.tools.autorelease.project.ProjectType.GRADLE
import no.elhub.tools.autorelease.project.ProjectType.MAVEN
import no.elhub.tools.autorelease.project.ProjectType.NPM
import no.elhub.tools.autorelease.reader.XmlDocumentReader.getMavenProjectVersion
import java.nio.file.Files
import java.nio.file.Files.createTempFile
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

object VersionFile {

    /**
     * Sets the [newVersion] in the [file] for a specified [projectType].
     */
    @OptIn(ExperimentalPathApi::class)
    fun setVersion(file: Path, projectType: ProjectType, newVersion: String) {
        val tempFile = createTempFile(Paths.get("."), null, null)
        when (projectType) {
            MAVEN -> {
                val versionNode = getMavenProjectVersion(file)
                versionNode.nodeValue = newVersion
                val xformer: Transformer = TransformerFactory.newInstance().newTransformer()
                xformer.transform(DOMSource(versionNode.ownerDocument), StreamResult(tempFile.toFile()))
                Files.delete(file)
                Files.move(tempFile, file)
            }
            GRADLE, NPM -> {
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
}
