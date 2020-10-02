import com.serebit.strife.buildsrc.configureForMavenCentral
import com.serebit.strife.buildsrc.createBintrayRepositories
import com.serebit.strife.buildsrc.fullPath
import com.serebit.strife.buildsrc.jarTask
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("multiplatform") version "1.4.10" apply false
    kotlin("plugin.serialization") version "1.4.10" apply false
    id("org.jetbrains.dokka") version "1.4.10"

    id("com.github.ben-manes.versions") version "0.33.0"
    `maven-publish`
}

allprojects {
    group = "com.serebit.strife"
    version = System.getenv("SNAPSHOT_VERSION") ?: "0.5.0"
    description = "An idiomatic Kotlin implementation of the Discord API"

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx/")
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
            tasks.dokkaHtml {
                outputDirectory.set(rootDir.resolve("public/docs"))
            }
        }

        // set jar base names to module paths, like strife-core and strife-samples-embeds
        tasks.withType<Jar>().configureEach { archiveBaseName.set(fullPath) }
        // enable junit 5 for tests
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
    }
}
