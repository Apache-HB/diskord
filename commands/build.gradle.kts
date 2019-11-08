import com.serebit.strife.buildsrc.implementation

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(project(":client"))
        implementation("com.serebit.logkat", "logkat", "0.4.7")
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        compilations["main"].defaultSourceSet.dependencies {
            implementation(kotlin("stdlib-jdk8"))
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
            externalDocumentationLink {
                url = file("$rootDir/public/docs/client/").toURI().toURL()
                packageListUrl = file("$rootDir/public/docs/client/package-list").toURI().toURL()
            }
        }
        register("jvm")
    }
}
