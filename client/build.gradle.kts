import com.serebit.strife.buildsrc.generateUnicodeEmoji
import com.serebit.strife.buildsrc.implementation
import com.serebit.strife.buildsrc.kotlinx
import com.serebit.strife.buildsrc.ktor

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlinx("serialization-json", "1.0.0"))
        implementation(ktor("client-cio", "1.4.1"))
        implementation("com.serebit.logkat", "logkat", "0.6.0")
        api(kotlinx("coroutines-core", "1.3.9-native-mt-2"))
        api(kotlinx("datetime", "0.1.0"))
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
        }
    }

    linuxX64()

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

generateUnicodeEmoji("${projectDir.absolutePath}/src/commonMain/kotlin/entities/")
