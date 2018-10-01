import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("kotlin-platform-common") version "1.2.71"
}

dependencies {
    compile(kotlin("stdlib-common"))
    compile(kotlin("reflect"))
    compile(kotlinx("coroutines-core", version = "0.30.0"))
}

kotlin.experimental.coroutines = Coroutines.ENABLE

fun kotlinx(module: String, version: String? = null): Any =
    "org.jetbrains.kotlinx:kotlinx-$module${version?.let { ":$version" } ?: ""}"
