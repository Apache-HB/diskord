import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.60"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.github.johnrengelman.shadow") version "2.0.4" apply false
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC8" apply false
    id("org.jetbrains.dokka") version "0.9.17" apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

repositories {
    jcenter()
}

subprojects {
    repositories {
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        sourceCompatibility = "1.8"
    }
}
