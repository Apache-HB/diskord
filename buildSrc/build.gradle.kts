plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.1")
}
