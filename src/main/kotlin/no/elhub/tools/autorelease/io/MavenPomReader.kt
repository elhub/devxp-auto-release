package no.elhub.tools.autorelease.io

import org.w3c.dom.Node
import org.w3c.dom.NodeList
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
object MavenPomReader {
    private val builder = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()

    private val xPath: XPath = XPathFactory.newInstance().newXPath().apply {
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

            override fun getPrefixes(namespaceURI: String?): MutableIterator<String> {
                throw UnsupportedOperationException("This operation is not supported")
            }
        }
    }

    /**
     * Looks up the `<project/>` node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getProject(file: Path): Node {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/ns:project"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node
    }

    /**
     * Looks up the `<module/>` under the `<modules/>` node in the xml [file] and returns as an instance of [NodeList].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getProjectModules(file: Path): NodeList {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/ns:project/ns:modules/ns:module"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET) as NodeList
    }

    /**
     * Looks up the `<version/>` under the `<parent/>` node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getProjectParentVersion(file: Path): Node {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/ns:project/ns:parent/ns:version/text()"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node
    }

    /**
     * Looks up the `<version/>` node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getProjectVersion(file: Path): Node? {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/ns:project/ns:version/text()"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node?
    }

    /**
     * Looks up the `<distributionManagement/>` node in the xml [file] and returns as an instance of [Node].
     *
     * It is expected that the [file] is a valid maven `pom.xml` file.
     */
    fun getProjectDistributionManagement(file: Path): Node? {
        val xmlDocument = builder.parse(file.toFile())
        val expression = "/ns:project/ns:distributionManagement"
        return xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node?
    }
}
