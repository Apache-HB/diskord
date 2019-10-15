
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
    sourceSets.commonMain.get().dependencies {
        implementation(kotlinx("serialization-runtime-native", version = "+"))
        api(kotlinx("coroutines-core-native", version = "+"))
        implementation(ktor("client-core-native", version = "+"))
        implementation(group = "com.serebit.logkat", name = "logkat", version = "+")
        api(group = "com.soywiz.korlibs.klock", name = "klock", version = "+")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(ktor("client-cio", version = "+"))
        }
        compilations["test"].defaultSourceSet.dependencies {
            implementation(kotlin("test-junit5"))
            implementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "+")
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
