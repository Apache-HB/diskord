import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.41"
    id("com.github.ben-manes.versions") version "0.17.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7"
    id("org.jetbrains.dokka") version "0.9.16"
}

group = "com.serebit"
version = "0.0.0"

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(group = "khttp", name = "khttp", version = "0.1.0")
    compile(group = "com.serebit", name = "loggerkt", version = "0.2.0")
    testCompile(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
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
        outputDirectory = "docs"
    }
}
