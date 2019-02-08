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
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("coroutines-core-common", version = Versions.coroutines))
        implementation(kotlinx("serialization-runtime-common", version = Versions.serialization))
        implementation(ktor("client-core", version = Versions.ktor))
        api(group = "com.serebit", name = "logkat-metadata", version = Versions.logkat)
    }
    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm().compilations["main"].defaultSourceSet.dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("coroutines-core", Versions.coroutines))
        implementation(kotlinx("serialization-runtime", Versions.serialization))
        implementation(ktor("client-okhttp", Versions.ktor))
        implementation(group = "org.http4k", name = "http4k-client-websocket", version = Versions.http4k)
        api(group = "com.serebit", name = "logkat-jvm", version = Versions.logkat)
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
