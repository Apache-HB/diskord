import com.serebit.strife.buildsrc.*

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlinx("serialization-runtime-native", version = Versions.SERIALIZATION))
        api(kotlinx("coroutines-core-native", version = Versions.COROUTINES))
        implementation(ktor("client-core-native", version = Versions.KTOR))
        implementation(group = "com.serebit.logkat", name = "logkat", version = Versions.LOGKAT)
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
            implementation(kotlin("test-junit5"))
            implementation(group = "org.junit.jupiter", name = "junit-jupiter", version = Versions.JUPITER)
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
