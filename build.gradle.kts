import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3-M2"
    id("com.github.ben-manes.versions") version "0.20.0"
}

repositories {
    jcenter()
}

subprojects {
    tasks.withType<KotlinCompile> {
        sourceCompatibility = "1.8"
    }
}
