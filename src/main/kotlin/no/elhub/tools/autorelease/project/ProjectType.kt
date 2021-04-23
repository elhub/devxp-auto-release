package no.elhub.tools.autorelease.project

@Suppress("unused")
enum class ProjectType {
    ANSIBLE {
        override val versionRegex: Regex? = null
        override val versionFormat: String = ""
        override val publishCommand: String = ""
        override val configFilePath: String = ""
    },
    GENERIC {
        override val versionRegex: Regex? = null
        override val versionFormat: String = ""
        override val publishCommand: String = ""
        override val configFilePath: String = ""
    },
    GRADLE {
        override val versionRegex = """^\s*version\s*=\s*.*""".toRegex()
        override val versionFormat = "version=%s"
        // TODO make skipping tests configurable?
        override val publishCommand = "./gradlew clean assemble publish -x test"
        override val configFilePath = "gradle.properties"
    },
    MAVEN {
        override val versionRegex = """^\s*<version>.*</version>\s*""".toRegex()
        override val versionFormat = "<version>%s</version>"
        // TODO make skipping tests configurable?
        override val publishCommand = "mvn clean package deploy -DskipTests -DfailIfNoTests=false"
        override val configFilePath = "pom.xml"
    },
    NPM {
        override val versionRegex = """^\s*"version"\s*:\s*".*"\s*,?""".toRegex()
        override val versionFormat = "\"version\": \"%s\","
        override val publishCommand = "npm publish"
        override val configFilePath = "package.json"
    };

    abstract val versionRegex: Regex?
    abstract val versionFormat: String
    abstract val publishCommand: String
    abstract val configFilePath: String

    override fun toString() = this.name.toLowerCase()
}
