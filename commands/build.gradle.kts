import com.serebit.strife.buildsrc.implementation

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(project(":client"))
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

    linuxX64()

    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
    }
}
