import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("multiplatform") version "1.3.21"
    id("kotlinx-serialization") version "1.3.21"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

kotlin {
    fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlinx("coroutines-core-common", version = "1.1.1"))
                implementation(kotlinx("serialization-runtime-common", version = "0.10.0"))
                implementation("io.ktor:ktor-client-core:1.1.2")
                api("com.serebit:logkat-metadata:0.4.2")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
    
    jvm().compilations["main"].defaultSourceSet.dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("serialization-runtime", version = "0.10.0"))
        implementation(kotlinx("coroutines-core", version = "1.1.1"))
        implementation("io.ktor:ktor-client-okhttp:1.1.2")
        implementation("org.http4k:http4k-client-websocket:3.112.1")
        api("com.serebit:logkat-jvm:0.4.2")
    }
    jvm().compilations["test"].defaultSourceSet.dependencies {
        implementation(kotlin("test-junit"))
    }
    
    targets.all {
        mavenPublication {
            artifactId = "${rootProject.name}-${project.name}-$targetName"
        }
    }
}

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
