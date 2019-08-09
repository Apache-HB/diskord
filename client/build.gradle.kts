import com.serebit.strife.gradle.*

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("serialization-runtime-native", version = Versions.SERIALIZATION))
        api(kotlinx("coroutines-core-native", version = Versions.COROUTINES))
        implementation(ktor("client-core-native", version = Versions.KTOR))
        implementation(group = "com.serebit", name = "logkat", version = Versions.LOGKAT)
        api(group = "com.soywiz.korlibs.klock", name = "klock", version = Versions.KLOCK)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(ktor("client-cio", version = Versions.KTOR))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
    }

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

tasks.dokka {
    outputDirectory = "$rootDir/public/docs"
    impliedPlatforms = mutableListOf("Common")

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
