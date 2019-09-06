import com.serebit.strife.gradle.*

plugins {
    kotlin("multiplatform") version "1.3.50" apply false
    id("kotlinx-serialization") version "1.3.50" apply false
    id("org.jetbrains.dokka") version "0.9.18" apply false

    id("com.github.ben-manes.versions") version "0.24.0"
    id("com.gradle.build-scan") version "2.4.1"
    `maven-publish`
}

allprojects {
    group = ProjectInfo.group
    version = ProjectInfo.version
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
                createBintrayRepository(System.getenv("BINTRAY_KEY"))

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
