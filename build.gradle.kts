import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm") version "1.2.50"
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7-2"
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.jfrog.bintray") version "1.8.3"
    id("maven-publish")
}

group = "com.serebit"
version = "0.0.0"

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.23.3")
    compile(group = "khttp", name = "khttp", version = "0.1.0")
    compile(group = "com.serebit", name = "loggerkt", version = "0.2.0")
    compile(group = "com.squareup.okhttp3", name = "okhttp", version = "3.10.0")
    compile(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.6")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.6")
    testCompile(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

detekt {
    profile("main", Action {
        input = "$projectDir/src/main/kotlin"
        config = "$projectDir/detekt.yml"
        filters = ".*test.*,.*/resources/.*,.*/tmp/.*"
    })
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<DokkaTask> {
        outputFormat = "html"
        outputDirectory = "public"
        apiVersion = version.toString()
    }

    withType<BintrayUploadTask> {
        doFirst {
            require(System.getenv("BINTRAY_KEY").isNotBlank())
        }
        dependsOn("build")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allJava.sourceDirectories.files)
}

bintray {
    user = "serebit"
    key = System.getenv("BINTRAY_KEY")
    setPublications("BintrayRelease")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "Maven"
        name = "diskord"
        vcsUrl = "https://gitlab.com/serebit/diskord.git"
        version.name = project.version.toString()
        setLicenses("Apache-2.0")
    })
}

publishing {
    publications.invoke {
        "BintrayRelease"(MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}
