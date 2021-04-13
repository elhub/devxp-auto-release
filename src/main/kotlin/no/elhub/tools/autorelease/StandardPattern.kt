package no.elhub.tools.autorelease

object StandardPattern {

    val gradleVersion = """^\s*version\s*=\s*.*""".toRegex()
    const val gradleFormat = "version=%s"
    const val gradlePublish = "./gradlew publish"
    val mavenVersion = """^\s*<version>.*</version>\s*""".toRegex()
    const val mavenFormat = "<version>%s</version>"
    const val mavenPublish = "./mvn publish"
}
