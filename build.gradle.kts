import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    id("no.elhub.devxp.kotlin-application") version "0.1.2"
    kotlin("plugin.serialization") version "2.1.20"
}

description = "Implement automated semantic release for gradle, maven and ansible projects."

val jgitVersion = "5.11.0.202103091610-r"

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.serialization.kotlinx.serialization.json)
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation(libs.cli.picocli)
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.slf4j.simple)
    implementation(libs.util.semver.ktrelease)
    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.mockk)
}

val applicationMainClass: String by project

application {
    mainClass.set(applicationMainClass)
}

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
}
listOf(tasks["distZip"], tasks["distTar"]).forEach {
    it.dependsOn(tasks["shadowJar"])
}
tasks["startScripts"].dependsOn(tasks["shadowJar"])
tasks["startShadowScripts"].dependsOn(tasks["jar"])

tasks["assemble"].dependsOn(tasks["shadowJar"])
