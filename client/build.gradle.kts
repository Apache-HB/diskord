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
        implementation(kotlinx("serialization-runtime-native"))
        implementation(ktor("client-core-native"))
        implementation("com.serebit.logkat", "logkat")
        api(kotlinx("coroutines-core-native"))
        api("com.soywiz.korlibs.klock", "klock")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(ktor("client-cio"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter", "junit-jupiter")
        }
    }

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}

tasks.withType<Test> { useJUnitPlatform() }

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
