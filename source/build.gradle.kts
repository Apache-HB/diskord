import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("multiplatform") version "1.3.20-eap-52"
    id("kotlinx-serialization") version "1.3.20-eap-52"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "com.serebit"
version = "0.0.0"

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"

kotlin {
    jvm()

    sourceSets {
        get("commonMain").dependencies {
            implementation(kotlin("stdlib-common"))
            implementation(kotlinx("coroutines-core-common", version = "1.1.0"))
            implementation(kotlinx("serialization-runtime-common", version = "0.10.0-eap-1"))
            api("com.serebit:logkat-metadata:0.4.2")
            implementation("io.ktor:ktor-client-core:1.1.1")
        }
        get("commonTest").dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
        get("jvmMain").dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlinx("coroutines-core", version = "1.1.0"))
            implementation(kotlinx("serialization-runtime", version = "0.10.0-eap-1"))
            api("com.serebit:logkat-jvm:0.4.2")
            implementation("io.ktor:ktor-client-okhttp:1.1.1")
            implementation("org.http4k:http4k-client-websocket:3.103.2")
        }
        get("jvmTest").dependencies {
            implementation(kotlin("test"))
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
