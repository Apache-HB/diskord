import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "4.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC9.2"
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlinx("coroutines-core", version = "0.27.0"))
    compile(group = "org.http4k", name = "http4k-client-okhttp", version = "3.38.1")
    compile(group = "org.http4k", name = "http4k-client-websocket", version = "3.38.1")
    compile(group = "com.serebit", name = "loggerkt", version = "0.3.0")
    compile(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.7")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.7")
    testCompile(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.10")
}

kotlin.experimental.coroutines = Coroutines.ENABLE

detekt.config = files("$projectDir/detekt.yml")

tasks {
    withType<DokkaTask> {
        outputDirectory = "$rootDir/public"
        doLast {
            file("$rootDir/public/${project.name}").renameTo(file("$rootDir/public/docs"))
        }
    }

    getByName<BintrayUploadTask>("bintrayUpload").doFirst {
        require(System.getenv("BINTRAY_KEY").isNotBlank())
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource.sourceDirectories.files)
}

publishing.publications.create<MavenPublication>("BintrayRelease") {
    from(components["java"])
    artifact(sourcesJar)
    groupId = project.group.toString()
    artifactId = rootProject.name
    version = project.version.toString()
}

bintray {
    user = "serebit"
    key = System.getenv("BINTRAY_KEY")
    setPublications("BintrayRelease")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "Maven"
        name = "diskord"
        version.name = project.version.toString()
    })
}

fun kotlinx(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"
