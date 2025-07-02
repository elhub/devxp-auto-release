plugins {
    id("no.elhub.devxp.kotlin-application") version "0.7.7"
    kotlin("plugin.serialization") version "2.1.21"
}

description = "Implement automated semantic release for gradle, maven and ansible projects."

val jgitVersion = "7.3.0.202506031305-r"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation("info.picocli:picocli:4.7.7")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("io.github.serpro69:semver.kt-release:0.13.0")

    testImplementation("commons-io:commons-io:2.19.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.mockk:mockk:1.14.4")
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
