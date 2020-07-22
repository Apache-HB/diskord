plugins {
    kotlin("plugin.serialization") version "1.3.72"
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", version = "1.3.72"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-native", "0.14.0")
}
