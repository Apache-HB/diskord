import com.serebit.strife.gradle.Versions
import com.serebit.strife.gradle.implementation

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(project(":core"))
        implementation(kotlin("stdlib-common"))
        implementation(group = "com.serebit", name = "logkat-metadata", version = Versions.LOGKAT)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm().compilations["main"].defaultSourceSet.dependencies {
        implementation(project(":core"))
        implementation(kotlin("stdlib-jdk8"))
        implementation(group = "com.serebit", name = "logkat-jvm", version = Versions.LOGKAT)
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
