package no.elhub.tools.autorelease.io

import org.w3c.dom.Document
import org.w3c.dom.DocumentType
import org.w3c.dom.Node
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.ExperimentalPathApi

object MavenPomWriter {
    private val transformer = TransformerFactory.newInstance().newTransformer()

    /**
     * Writes the xml [Document] associated with `this` receiver [Node].
     *
     * *When `this` [Node] is a [Document] or a [DocumentType] which is not used with any [Document] yet,
     * this is null*
     */
    @OptIn(ExperimentalPathApi::class)
    fun Node.writeTo(file: Path) {
        val tempFile = Files.createTempFile(Paths.get("."), null, null)
        val doc = requireNotNull(ownerDocument) { "Owner document for $this node is null" }
        transformer.transform(DOMSource(doc), StreamResult(tempFile.toFile()))
        Files.delete(file)
        Files.move(tempFile, file)
    }
}
