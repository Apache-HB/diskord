import com.serebit.strife.buildsrc.*

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    id("org.jetbrains.dokka") apply false

    id("de.fayard.refreshVersions")
    id("com.gradle.build-scan")
    `maven-publish`
}

allprojects {
    group = ProjectInfo.group
    version = System.getenv("SNAPSHOT_VERSION") ?: ProjectInfo.version
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
            publishing {
                createBintrayRepositories()

                val javadocJar by tasks.creating(Jar::class) {
                    archiveClassifier.value("javadoc")
                }

                val sourcesJar by tasks.creating(Jar::class) {
                    archiveClassifier.value("sources")
                }

                publications.withType<MavenPublication>().all {
                    // replace project names in artifact with their module paths, ie core-jvm becomes strife-core-jvm
                    artifactId = artifactId.replace(this@subprojects.name, fullPath)

                    // configure additional POM data for Maven Central
                    configureForMavenCentral(javadocJar, sourcesJar)
                }
            }
        }

        tasks.withType<Jar> {
            // set jar base names to module paths, like strife-core and strife-samples-embeds
            archiveBaseName.set(fullPath)
        }
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlwaysIf(System.getenv("PUBLISH_BUILD_SCAN")?.toBoolean() == true)
}
