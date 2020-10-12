import com.serebit.strife.buildsrc.implementation

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin{
    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(project(":client"))
            implementation(kotlin("scripting-jvm-host"))
            implementation("com.serebit.logkat", "logkat", "0.6.0")
            api(kotlin("scripting-jvm"))
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
