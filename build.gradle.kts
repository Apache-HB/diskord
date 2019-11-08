import com.serebit.strife.buildsrc.*
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("multiplatform") version "1.3.50" apply false
    kotlin("plugin.serialization") version "1.3.50" apply false
    id("org.jetbrains.dokka") version "0.10.0" apply false

    id("com.github.ben-manes.versions") version "0.27.0"
    `maven-publish`
}

allprojects {
    group = "com.serebit.strife"
    version = System.getenv("SNAPSHOT_VERSION") ?: "0.4.0-SNAPSHOT"
    description = "An idiomatic Kotlin implementation of the Discord API"
}

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
            publishing.createBintrayRepositories()

            val javadocJar by jarTask()
            val sourcesJar by jarTask()

            publishing.publications.withType<MavenPublication>().all {
                // replace project names in artifact with their module paths, ie core-jvm becomes strife-core-jvm
                artifactId = artifactId.replace(this@subprojects.name, fullPath)

                // configure additional POM data for Maven Central
                configureForMavenCentral(javadocJar, sourcesJar)
            }
        }

        // set jar base names to module paths, like strife-core and strife-samples-embeds
        tasks.withType<Jar> { archiveBaseName.set(fullPath) }
        // enable junit 5 for tests
        tasks.withType<Test> { useJUnitPlatform() }
    }
}
