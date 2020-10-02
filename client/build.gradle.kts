import com.serebit.strife.buildsrc.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlinx("serialization-json", "1.0.0-RC2"))
        implementation(ktor("client-core", "1.4.1"))
        implementation("com.serebit.logkat", "logkat", "0.6.0")
        api(kotlinx("coroutines-core", "1.3.9"))
        api(kotlinx("datetime", "0.1.0"))
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(ktor("client-cio", "1.4.1"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
        }
    }

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

generateUnicodeEmoji("${projectDir.absolutePath}/src/commonMain/kotlin/entities/")
