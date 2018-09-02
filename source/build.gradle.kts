import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC8"
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

repositories {
    jcenter()
    maven("http://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile(kotlinx("coroutines-core", version = "0.25.2-eap13"))
    compile(group = "org.http4k", name = "http4k-client-okhttp", version = "3.37.0")
    compile(group = "org.http4k", name = "http4k-client-websocket", version = "3.37.0")
    compile(group = "com.serebit", name = "loggerkt", version = "0.3.0")
    compile(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.6")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.6")
    testCompile(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
}

detekt.defaultProfile {
    input = "$projectDir/src/main/kotlin"
    config = "$projectDir/detekt.yml"
    filters = ".*test.*,.*/resources/.*,.*/tmp/.*"
}

tasks {
    withType<DokkaTask> {
        outputDirectory = "public"
        doLast { file("public/${project.name}").renameTo(file("public/docs")) }
    }

    withType<BintrayUploadTask> {
        doFirst { require(System.getenv("BINTRAY_KEY").isNotBlank()) }
        dependsOn("build")
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
    artifactId = project.name
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
