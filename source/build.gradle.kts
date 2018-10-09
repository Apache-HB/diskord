import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.3.0-rc-146"
    id("com.jfrog.bintray") version "1.8.4"
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
        implementation(group = "io.ktor", name = "ktor-client", version = "1.0.0-alpha-1")
    }
    create("jvmMain") {
        dependsOn(getByName("commonMain"))
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlinx("coroutines-core", version = "0.30.2-eap13"))
            implementation(group = "com.serebit", name = "logkat-jvm", version = "0.4.1-eap13")
            implementation(group = "io.ktor", name = "ktor-client-cio", version = "1.0.0-alpha-1")
            implementation(group = "org.http4k", name = "http4k-client-websocket", version = "3.38.1")
            implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.7")
            implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.7")
        }
    }
    create("jvmTest") {
        dependsOn(getByName("commonTest"))
        dependencies {
            implementation(group = "io.kotlintest", name = "kotlintest-runner-jvm", version = "3.1.10")
        }
    }
}

apply(from = "$rootDir/gradle/platform-targets.gradle")

bintray {
    user = "serebit"
    key = System.getenv("BINTRAY_KEY")
    setPublications("jvm")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "test"
        name = rootProject.name
        version.name = project.version.toString()
    })
}
