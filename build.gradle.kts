import com.serebit.strife.gradle.configureBintray
import com.serebit.strife.gradle.kotlinx

plugins {
    kotlin("multiplatform") version "1.3.41" apply false
    id("kotlinx-serialization") version "1.3.41" apply false
    id("org.jetbrains.dokka") version "0.9.18" apply false

    id("com.github.ben-manes.versions") version "0.22.0"
    id("com.gradle.build-scan") version "2.4"
    `maven-publish`
}

allprojects {
    group = "com.serebit.strife"
    version = "0.2.0"
}

subprojects {
    repositories {
        jcenter()
        kotlinx()
    }

    val fullPath = "${rootProject.name}${project.path.replace(":", "-")}"

    // has to evaluate after the rest of the project build script to catch all configured tasks and artifacts
    afterEvaluate {
        tasks.withType<Jar> {
            // set jar base names to module paths, like strife-core and strife-samples-embeds
            archiveBaseName.set(fullPath)
        }
    }

    // will only run in subprojects with the maven-publish plugin already applied
    pluginManager.withPlugin("maven-publish") {
        publishing.configureBintray("serebit", "public", rootProject.name, System.getenv("BINTRAY_KEY"))

        afterEvaluate {
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

    publishAlwaysIf(System.getenv("PUBLISH_BUILD_SCAN")?.toBoolean() == true)
}
