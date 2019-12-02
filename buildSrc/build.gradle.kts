plugins {
    kotlin("plugin.serialization") version "1.3.61"
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", version = "1.3.61"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-native", "0.14.0")
}
