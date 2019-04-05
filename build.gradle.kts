import com.serebit.strife.gradle.kotlinEap
import com.serebit.strife.gradle.kotlinx
import com.serebit.strife.gradle.ktor
import com.serebit.strife.gradle.soywiz
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("multiplatform") version "1.3.30-eap-125" apply false
    id("org.jetbrains.dokka") version "0.9.18" apply false
    id("kotlinx-serialization") version "1.3.30-eap-125" apply false
    id("com.jfrog.bintray") version "1.8.4" apply false

    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.gradle.build-scan") version "2.2.1"
    `maven-publish`
}

subprojects {
    group = "com.serebit"
    version = "PEPE_SILVIA"

    repositories {
        jcenter()
        kotlinx()
        kotlinEap()
        ktor()
        soywiz()
    }

    // has to evaluate after the rest of the project build script to catch all configured tasks and artifacts
    afterEvaluate {
        val fullPath = "${rootProject.name}${project.path.replace(":", "-")}"

        tasks.withType<Jar> {
            // set jar base names to module paths, like strife-core and strife-samples-embeds
            archiveBaseName.set(fullPath)
        }

        // will only run in subprojects with the maven-publish plugin already applied
        pluginManager.withPlugin("maven-publish") {
            publishing.publications.filterIsInstance<MavenPublication>().forEach {
                // replace project names in artifact with their module paths, ie core-jvm becomes strife-core-jvm
                it.artifactId = it.artifactId.replace(name, fullPath)
            }
        }
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlwaysIf(System.getenv("PUBLISH_BUILD_SCAN") == "true")
}
