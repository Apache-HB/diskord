import com.jfrog.bintray.gradle.BintrayExtension
import com.serebit.strife.gradle.*

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("com.jfrog.bintray")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        // Kotlin
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("coroutines-core-common", version = Versions.COROUTINES))
        implementation(kotlinx("serialization-runtime-common", version = Versions.SERIALIZATION))
        // Web
        implementation(ktor("client-core", version = Versions.KTOR))
        // Util
        api(group = "com.serebit", name = "logkat-metadata", version = Versions.LOGKAT)
        api(group = "com.soywiz", name = "klock-metadata", version = Versions.KLOCK)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm().compilations["main"].defaultSourceSet.dependencies {
        // Kotlin
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("coroutines-core", version = Versions.COROUTINES))
        implementation(kotlinx("serialization-runtime", version = Versions.SERIALIZATION))
        // Web
        implementation(ktor("client-okhttp", version = Versions.KTOR))
        implementation(group = "org.http4k", name = "http4k-client-websocket", version = Versions.HTTP4K)
        // Util
        api(group = "com.serebit", name = "logkat-jvm", version = Versions.LOGKAT)
        api(group = "com.soywiz", name = "klock-jvm", version = Versions.KLOCK)
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
