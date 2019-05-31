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
        // Util
        implementation(group = "com.serebit", name = "logkat-metadata", version = Versions.LOGKAT)
        api(kotlinx("coroutines-core-common", version = Versions.COROUTINES))
        api(group = "com.soywiz", name = "klock-metadata", version = Versions.KLOCK)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("com.gitlab.JonoAugustine:KPack:a098e9b2")
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            // Kotlin
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlinx("serialization-runtime", version = Versions.SERIALIZATION))
            api(kotlinx("coroutines-core", version = Versions.COROUTINES))
            // Web
            implementation(ktor("client-cio", version = Versions.KTOR))
            // Util
            implementation(group = "com.serebit", name = "logkat-jvm", version = Versions.LOGKAT)
            api(group = "com.soywiz", name = "klock-jvm", version = Versions.KLOCK)
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
    }

    sourceSets.forEach {
        it.languageSettings.apply {
            progressiveMode = true
            useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

tasks.dokka {
    outputDirectory = "$rootDir/public/docs"
    impliedPlatforms = mutableListOf("Common")

    // tell dokka about the JVM task, so that it can resolve all our dependencies
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
