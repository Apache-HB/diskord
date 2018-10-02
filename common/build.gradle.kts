import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("kotlin-platform-common") version "1.2.71"
}

dependencies {
    compile(kotlin("stdlib-common"))
    compile(kotlin("reflect"))
    compile(kotlinx("coroutines-core-common", version = "0.30.0"))
    compile(group = "com.serebit", name = "logkat-common", version = "0.4.1")
    compile(group = "io.ktor", name = "ktor-client", version = "0.9.5")
}

kotlin.experimental.coroutines = Coroutines.ENABLE

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
