import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.3.20-eap-25"
    id("kotlinx-serialization") version "1.3.20-eap-25"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

kotlin {
    targets {
        jvm()
    }

    sourceSets {
        get("commonMain").dependencies {
            implementation(kotlin("stdlib-common"))
            implementation(kotlinx("coroutines-core-common", version = "1.0.1"))
            implementation(kotlinx("serialization-runtime-common", version = "0.9.1"))
            implementation(group = "com.serebit", name = "logkat-metadata", version = "0.4.2")
            implementation(group = "io.ktor", name = "ktor-client", version = "1.0.1")
        }
        get("commonTest").dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
        get("jvmMain").dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlinx("coroutines-core", version = "1.0.1"))
            implementation(kotlinx("serialization-runtime", version = "0.9.1"))
            implementation(group = "com.serebit", name = "logkat-jvm", version = "0.4.2")
            implementation(group = "io.ktor", name = "ktor-client-okhttp", version = "1.0.1")
            implementation(group = "org.http4k", name = "http4k-client-websocket", version = "3.103.2")
        }
        get("jvmTest").dependencies {
            implementation(kotlin("test-junit5"))
        }
    }
}

bintray {
    user = "serebit"
    key = System.getenv("BINTRAY_KEY")
    setPublications("jvm")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "public"
        name = rootProject.name
        version.name = project.version.toString()
    })
}
