description = "Implement automated semantic release for gradle, maven and ansible projects."

val kotestVersion = "4.4.1"
val jgitVersion = "5.11.0.202103091610-r"
val mockkVersion = "1.10.6"

repositories {
    mavenCentral()
    maven("https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation("info.picocli:picocli:4.6.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("io.github.serpro69:semver.kt-release:0.4.0")
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-allure-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

val mainClassName = "no.elhub.tools.autorelease.AutoReleaseKt"

/*
 * Publishing
 * - Create a fat jar for deployment
 * - Run it after the jar command and as part of the assemble task
 */
val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set(rootProject.name)
    manifest {
        attributes["Implementation-Title"] = rootProject.name
        attributes["Implementation-Version"] = rootProject.version
        attributes["Main-Class"] = mainClassName
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    with(tasks.jar.get() as CopySpec)
    mustRunAfter(tasks["jar"])
}

tasks["assemble"].dependsOn(tasks["fatJar"])
