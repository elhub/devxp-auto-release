plugins {
    id("no.elhub.devxp.kotlin-application") version "0.1.0"
}

description = "Implement automated semantic release for gradle, maven and ansible projects."

val kotestVersion = "4.4.1"
val jgitVersion = "5.11.0.202103091610-r"
val mockkVersion = "1.10.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation("info.picocli:picocli:4.6.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("io.github.serpro69:semver.kt-release:0.4.3")
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
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
