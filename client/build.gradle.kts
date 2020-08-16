import com.serebit.strife.buildsrc.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlinx("serialization-core", "1.0.0-RC"))
        implementation(ktor("client-core", "1.3.2-1.4.0-rc"))
        implementation("com.serebit.logkat", "logkat", "0.6.0")
        api(kotlinx("coroutines-core", "1.3.9"))
        api("com.soywiz.korlibs.klock", "klock", "1.12.0")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(ktor("client-cio", "1.3.2-1.4.0-rc"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter", "junit-jupiter", "5.6.2")
        }
    }

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

generateUnicodeEmoji("${projectDir.absolutePath}/src/commonMain/kotlin/entities/")
