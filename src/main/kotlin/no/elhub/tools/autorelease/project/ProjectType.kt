package no.elhub.tools.autorelease.project

@Suppress("unused")
enum class ProjectType {
    ANSIBLE {
        override val versionRegex: Regex = """^version:\s.*""".toRegex()
        override val versionFormat: String = "version: %s"
        override val publishCommand: String = ""
        override val configFilePath: String = "galaxy.yml"
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
        override val versionFormat = "%s"
        // TODO make skipping tests configurable?
        override val publishCommand = "mvn clean deploy -DskipTests -DfailIfNoTests=false"
        override val configFilePath = "pom.xml"
    },
    NPM {
        override val versionRegex = """^\s*"version"\s*:\s*".*"\s*,?""".toRegex()
        override val versionFormat = "%s"
        override val publishCommand = "npm run release"
        override val configFilePath = "package.json"
    };

    abstract val versionRegex: Regex?
    abstract val versionFormat: String
    abstract val publishCommand: String
    abstract val configFilePath: String

    override fun toString() = this.name.toLowerCase()
}
