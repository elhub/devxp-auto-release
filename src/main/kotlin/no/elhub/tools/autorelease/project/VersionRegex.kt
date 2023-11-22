package no.elhub.tools.autorelease.project

enum class VersionRegex(val regex: Regex, val versionFormat: String) {
    ANSIBLE(regex = """^version:\s.*""".toRegex(), versionFormat = "version: %s"),
    GRADLE(regex = """^\s*version\s*=\s*.*""".toRegex(), versionFormat = "version=%s"),
    MAVEN(regex = """^\s*<version>.*</version>\s*""".toRegex(), versionFormat = "%s"),
    NPM(regex = """^\s*"version"\s*:\s*".*"\s*,?""".toRegex(), versionFormat = "%s")
}
