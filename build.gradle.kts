import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.70"
    id("com.github.ben-manes.versions") version "0.20.0"
}

allprojects {
    repositories {
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        sourceCompatibility = "1.8"
    }

}
