plugins {
    alias(libs.plugins.elhub.gradle)
    alias(libs.plugins.serialization)
}

description = "Implement automated semantic release for gradle, maven and ansible projects."

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.git.jgit)
    implementation(libs.git.jgit.ssh)
    implementation(libs.cli.picocli)
    implementation(libs.bundles.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.serpro69.semver)
    testImplementation(libs.apache.commons.io)
    implementation(libs.test.kotest.runner.junit5)
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
