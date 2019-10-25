import com.serebit.strife.buildsrc.configureForMavenCentral
import com.serebit.strife.buildsrc.createBintrayRepositories
import com.serebit.strife.buildsrc.fullPath
import com.serebit.strife.buildsrc.kotlinx
import org.gradle.jvm.tasks.Jar as JarTask

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    id("org.jetbrains.dokka") apply false

    id("de.fayard.refreshVersions")
    `maven-publish`
}

allprojects {
    group = "com.serebit.strife"
    version = System.getenv("SNAPSHOT_VERSION") ?: "0.3.0"
    description = "An idiomatic Kotlin implementation of the Discord API"
}

publishing.createBintrayRepositories()

subprojects {
    repositories {
        mavenCentral()
        jcenter()
        kotlinx()
    }

    // has to evaluate after the rest of the project build script to catch all configured tasks and artifacts
    afterEvaluate {
        // will only run in subprojects with the maven-publish plugin already applied
        pluginManager.withPlugin("maven-publish") {
            val javadocJar by tasks.creating(Jar::class) {
                archiveClassifier.value("javadoc")
            }

            val sourcesJar by tasks.creating(Jar::class) {
                archiveClassifier.value("sources")
            }

            publishing.publications.withType<MavenPublication>().all {
                // replace project names in artifact with their module paths, ie core-jvm becomes strife-core-jvm
                artifactId = artifactId.replace(this@subprojects.name, fullPath)

                // configure additional POM data for Maven Central
                configureForMavenCentral(javadocJar, sourcesJar)
            }
        }

        tasks.withType<JarTask> {
            // set jar base names to module paths, like strife-core and strife-samples-embeds
            archiveBaseName.set(fullPath)
        }
    }
}
