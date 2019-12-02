import com.serebit.strife.buildsrc.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("serialization-runtime-native", "0.14.0"))
        implementation(ktor("client-core-native", "1.3.0-beta-2"))
        implementation("com.serebit.logkat", "logkat", "0.5.2")
        api(kotlinx("coroutines-core-native", "1.3.2-1.3.60"))
        api("com.soywiz.korlibs.klock", "klock", "1.8.0")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(ktor("client-cio", "1.3.0-beta-2"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter", "junit-jupiter", "5.5.2")
        }
    }

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

tasks.dokka {
    outputDirectory = "$rootDir/public/docs"

    multiplatform {
        register("jvm") { skipEmptyPackages = true }
    }
}

generateUnicodeEmoji("${projectDir.absolutePath}/src/commonMain/kotlin/entities/")
