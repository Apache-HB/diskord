import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform") version "1.3.0-rc-198"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

fun kotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module"
fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

kotlin.sourceSets {
    getByName("commonMain").dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("coroutines-core-common", version = "1.0.0-RC1"))
        implementation(group = "com.serebit", name = "logkat-metadata", version = "0.4.2")
        implementation(group = "io.ktor", name = "ktor-client", version = "1.0.0-beta-2")
    }
    create("jvmMain") {
        dependsOn(getByName("commonMain"))
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlinx("coroutines-core", version = "1.0.0-RC1"))
            implementation(group = "com.serebit", name = "logkat-jvm", version = "0.4.2")
            implementation(group = "io.ktor", name = "ktor-client-cio", version = "1.0.0-beta-2")
            implementation(group = "org.http4k", name = "http4k-client-websocket", version = "3.93.4")
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
    setPublications("metadata", "jvm")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "public"
        name = rootProject.name
        version.name = project.version.toString()
    })
}
