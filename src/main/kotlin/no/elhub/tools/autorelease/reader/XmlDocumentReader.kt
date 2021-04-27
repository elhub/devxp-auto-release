package no.elhub.tools.autorelease.reader

import org.w3c.dom.Node
import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

// TODO should do the same for json (NPM)
/**
 * Provides functions for reading xml documents.
 */
object XmlDocumentReader {
    private val builder = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()

    /**
     * Looks up the `<version/>` node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getMavenProjectVersion(file: Path): Node {
        val xmlDocument = builder.parse(file.toFile())
        val xPath: XPath = XPathFactory.newInstance().newXPath().apply {
            namespaceContext = object : NamespaceContext {
                override fun getNamespaceURI(prefix: String?): String {
                    return when (prefix) {
                        "ns" -> "http://maven.apache.org/POM/4.0.0"
                        else -> XMLConstants.NULL_NS_URI
                    }
                }

                override fun getPrefix(namespaceURI: String?): String {
                    throw UnsupportedOperationException("This operation is not supported")
                }

                override fun getPrefixes(namespaceURI: String?): MutableIterator<Any> {
                    throw UnsupportedOperationException("This operation is not supported")
                }
            }
        }
        val expression = "/ns:project/ns:version/text()"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node
    }
}
