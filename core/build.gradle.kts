import com.serebit.strife.gradle.*

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        // Kotlin
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("serialization-runtime-common", version = Versions.SERIALIZATION))
        // Web
        implementation(ktor("client-core", version = Versions.KTOR))
        implementation(ktor("client-websocket", version = Versions.KTOR))
        // Util
        implementation(group = "com.serebit", name = "logkat-metadata", version = Versions.LOGKAT)
        api(kotlinx("coroutines-core-common", version = Versions.COROUTINES))
        api(group = "com.soywiz", name = "klock-metadata", version = Versions.KLOCK)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("com.gitlab.JonoAugustine:KPack:29c2e60a")
    }

    jvm().compilations["main"].defaultSourceSet.dependencies {
        // Kotlin
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("serialization-runtime", version = Versions.SERIALIZATION))
        api(kotlinx("coroutines-core", version = Versions.COROUTINES))
        // Web
        implementation(ktor("client-cio", version = Versions.KTOR))
        implementation(ktor("client-okhttp", version = Versions.KTOR))
        implementation(ktor("client-websocket-jvm", version = Versions.KTOR))
        // Util
        implementation(group = "com.serebit", name = "logkat-jvm", version = Versions.LOGKAT)
        api(group = "com.soywiz", name = "klock-jvm", version = Versions.KLOCK)
    }
    jvm().compilations["test"].defaultSourceSet.dependencies {
        implementation(kotlin("test-junit"))
    }
}

tasks.dokka {
    outputDirectory = "$rootDir/public/docs"
    impliedPlatforms = mutableListOf("Common")

    // required so dokka doesn't crash on parsing multiplatform source sets, add them manually later
    kotlinTasks { emptyList() }

    sourceRoot {
        path = kotlin.sourceSets.commonMain.get().kotlin.srcDirs.single().absolutePath
        platforms = listOf("Common")
    }

    sourceRoot {
        path = kotlin.jvm().compilations["main"].defaultSourceSet.kotlin.srcDirs.single().absolutePath
        platforms = listOf("JVM")
    }
}
