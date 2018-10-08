import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.3.0-rc-146"
    id("com.jfrog.bintray") version "1.8.4" apply false
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

tasks.withType<Jar> { baseName = "diskord" }

fun kotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module"
fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

kotlin.sourceSets {
    getByName("commonMain").dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlin("reflect"))
        implementation(kotlinx("coroutines-core-common", version = "0.30.2-eap13"))
        implementation(group = "com.serebit", name = "logkat-common", version = "0.4.1-eap13")
        implementation(group = "io.ktor", name = "ktor-client", version = "0.9.6-alpha-1-rc13")
    }
    create("jvmMain").dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("coroutines-core", version = "0.30.2-eap13"))
        implementation(group = "com.serebit", name = "logkat-jvm", version = "0.4.1-eap13")
        implementation(group = "io.ktor", name = "ktor-client-cio", version = "0.9.6-alpha-1-rc13")
        implementation(group = "org.http4k", name = "http4k-client-websocket", version = "3.38.1")
        implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.7")
        implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.7")
    }
    create("jvmTest").dependencies {
        implementation(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.10")
    }
}

apply(from = "$rootDir/gradle/platform-targets.gradle")

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    val sourcesJar by tasks.creating(Jar::class) {
        classifier = "sources"
        from(sourceSets["main"].allSource.sourceDirectories.files)
    }

    publishing.publications.create<MavenPublication>("BintrayRelease") {
        from(components["java"])
        artifact(sourcesJar)
        groupId = rootProject.group.toString()
        artifactId = "${rootProject.name}-${project.name}"
        version = rootProject.version.toString()
    }

    apply(from = "$rootDir/gradle/bintray-publish.gradle")
}
