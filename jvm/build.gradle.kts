import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("kotlin-platform-jvm") version "1.2.71"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC9.2"
    id("org.jetbrains.dokka") version "0.9.17"
}

dependencies {
    expectedBy(project(":common"))
    compile(kotlin("stdlib-jdk8"))
    compile(kotlinx("coroutines-core", version = "0.30.0"))
    compile(group = "io.ktor", name = "ktor-client-cio", version = "0.9.5")
    compile(group = "org.http4k", name = "http4k-client-websocket", version = "3.38.1")
//    compile(group = "com.serebit", name = "loggerkt", version = "0.3.0")
    compile(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.9.7")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.7")
    testCompile(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.10")
}

kotlin.experimental.coroutines = Coroutines.ENABLE

detekt.config = files("$projectDir/detekt.yml")

tasks {
    withType<KotlinCompile> { sourceCompatibility = "1.8" }

    withType<DokkaTask> {
        outputDirectory = "$rootDir/public"
        moduleName = "docs"
    }
}

fun kotlinx(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"
