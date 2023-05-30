package no.elhub.tools.autorelease.io

import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import no.elhub.tools.autorelease.config.Field
import org.w3c.dom.Node
import org.w3c.dom.NodeList

/**
 * Provides functions for reading xml documents.
 */
abstract class XmlReader(private val namespace: String) {
    protected val builder: DocumentBuilder = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()

    protected val xPath: XPath = XPathFactory.newInstance().newXPath().apply {
        namespaceContext = object : NamespaceContext {
            override fun getNamespaceURI(prefix: String?): String {
                return when (prefix) {
                    "ns" -> namespace
                    else -> XMLConstants.NULL_NS_URI
                }
            }

            override fun getPrefix(namespaceURI: String?): String {
                throw UnsupportedOperationException("This operation is not supported")
            }

            override fun getPrefixes(namespaceURI: String?): MutableIterator<String> {
                throw UnsupportedOperationException("This operation is not supported")
            }
        }
    }

    /**
     * Looks up the [field] node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getField(file: Path, field: Field): Node? {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/${getExpression(field)}"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node?
    }

    /**
     * Looks up the [field] node in the xml [file] and returns as an instance of [NodeList].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getFields(file: Path, field: Field): NodeList {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/${getExpression(field)}"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET) as NodeList
    }

    private fun getExpression(field: Field): String = field.parent?.let { "${getExpression(it)}/ns:${field.name}" }
        ?: "ns:${field.name}"

}
