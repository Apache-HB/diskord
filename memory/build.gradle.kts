import com.serebit.strife.buildsrc.implementation
import com.serebit.strife.buildsrc.kotlinx

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(project(":client"))
        implementation(kotlinx("serialization-json", "1.0.0-RC2"))
        implementation("com.serebit.logkat", "logkat", "0.6.0")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm().compilations["test"].defaultSourceSet.dependencies {
        implementation(kotlin("test-junit5"))
        implementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
    }
}
