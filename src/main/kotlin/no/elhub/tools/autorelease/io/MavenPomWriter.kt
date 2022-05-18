package no.elhub.tools.autorelease.io

import org.w3c.dom.Document
import org.w3c.dom.DocumentType
import org.w3c.dom.Node
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object MavenPomWriter {
    private val transformer = TransformerFactory.newInstance().newTransformer().also {
        it.setOutputProperty(OutputKeys.INDENT, "yes")
    }

    /**
     * Writes the xml [Document] associated with `this` receiver [Node].
     *
     * *When `this` [Node] is a [Document] or a [DocumentType] which is not used with any [Document] yet,
     * this is null*
     */
    fun Node.writeTo(file: Path) {
        val tempFile = Files.createTempFile(Paths.get("."), null, null)
        val doc = requireNotNull(ownerDocument) { "Owner document for $this node is null" }
        transformer.transform(DOMSource(doc), StreamResult(tempFile.toFile()))
        Files.delete(file)
        Files.move(tempFile, file)
    }

    /**
     * Appends the distributionManagement child node to this `project` receiver node and returns it.
     *
     * @return this receiver node
     * @throws IllegalArgumentException if the receiver is not a `project` node.
     */
    fun Node.appendDistributionManagement(
        repository: Repository,
        snapshotRepository: Repository,
    ): Node {
        return if (nodeName == "project") {
            val doc = ownerDocument
            val dm = doc.createElement("distributionManagement")
            appendChild(dm)
            val repo = doc.createElement("repository")
            dm.appendChild(repo)
            repo.appendRepoElements(repository)
            val snapshotRepo = doc.createElement("snapshotRepository")
            dm.appendChild(snapshotRepo)
            snapshotRepo.appendRepoElements(snapshotRepository)
            this
        } else throw IllegalArgumentException("Node $nodeName is not a 'project' node.")
    }
}

private fun Node.appendRepoElements(repository: Repository) {
    val uniqueVersion = ownerDocument.createElement("uniqueVersion")
    appendChild(uniqueVersion)
    uniqueVersion.textContent = repository.uniqueVersion.toString()
    val id = ownerDocument.createElement("id")
    appendChild(id)
    id.textContent = repository.id
    val name = ownerDocument.createElement("name")
    appendChild(name)
    name.textContent = repository.name
    val url = ownerDocument.createElement("url")
    appendChild(url)
    url.textContent = repository.url
    val layout = ownerDocument.createElement("layout")
    appendChild(layout)
    layout.textContent = repository.layout
}

data class Repository(
    val uniqueVersion: Boolean,
    val id: String,
    val name: String,
    val url: String,
    val layout: String = "default"
)
