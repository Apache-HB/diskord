import com.serebit.strife.buildsrc.api
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
    // versions can be found in gradle.properties
    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("serialization-runtime-native", "0.13.0"))
        implementation(ktor("client-core-native", "1.2.5"))
        implementation("com.serebit.logkat", "logkat", "0.4.7")
        api(kotlinx("coroutines-core-native", "1.3.2"))
        api("com.soywiz.korlibs.klock", "klock", "1.7.5")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(ktor("client-cio", "1.2.5"))
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
        register("global") {
            perPackageOption {
                prefix = "com.serebit.strife.internal"
                suppress = true
            }
        }
        register("jvm")
    }
}
