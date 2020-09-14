plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-core", "1.0.0-RC")
}
