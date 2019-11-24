plugins {
    kotlin("plugin.serialization") version "1.3.60"
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", version = "1.3.60"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-native", "0.14.0")
}
