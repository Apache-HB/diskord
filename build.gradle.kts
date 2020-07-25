import com.serebit.strife.buildsrc.configureForMavenCentral
import com.serebit.strife.buildsrc.createBintrayRepositories
import com.serebit.strife.buildsrc.fullPath
import com.serebit.strife.buildsrc.jarTask
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("multiplatform") version "1.3.72" apply false
    kotlin("plugin.serialization") version "1.3.72" apply false
    id("org.jetbrains.dokka") version "0.10.1"

    id("com.github.ben-manes.versions") version "0.29.0"
    `maven-publish`
}

allprojects {
    group = "com.serebit.strife"
    version = System.getenv("SNAPSHOT_VERSION") ?: "0.4.0"
    description = "An idiomatic Kotlin implementation of the Discord API"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    // has to evaluate after the rest of the project build script to catch all configured tasks and artifacts
    afterEvaluate {
        // will only run in subprojects with the maven-publish plugin already applied
        pluginManager.withPlugin("maven-publish") {
            publishing.createBintrayRepositories()

            val javadocJar by jarTask()
            val kmpSourcesJar by jarTask()

            publishing.publications.withType<MavenPublication>().all {
                // replace project names in artifact with their module paths, ie core-jvm becomes strife-core-jvm
                artifactId = artifactId.replace(this@subprojects.name, fullPath)

                // configure additional POM data for Maven Central
                configureForMavenCentral(javadocJar, kmpSourcesJar)
            }
        }

        pluginManager.withPlugin("org.jetbrains.dokka") {
            tasks.dokka {
                outputDirectory = "$rootDir/public/docs"

                multiplatform {
                    register("jvm")
                }
            }
        }

        // set jar base names to module paths, like strife-core and strife-samples-embeds
        tasks.withType<Jar> { archiveBaseName.set(fullPath) }
        // enable junit 5 for tests
        tasks.withType<Test> { useJUnitPlatform() }
    }
}
