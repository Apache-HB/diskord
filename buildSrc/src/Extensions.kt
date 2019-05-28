package com.serebit.strife.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"
fun KotlinDependencyHandler.api(group: String, name: String, version: String) = api("$group:$name:$version")
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

fun DependencyHandler.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun DependencyHandler.ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"

fun RepositoryHandler.kotlinx() = maven("https://kotlin.bintray.com/kotlinx")
fun RepositoryHandler.kotlinEap() = maven("https://kotlin.bintray.com/kotlin-eap")
fun RepositoryHandler.kotlinDev() = maven("https://kotlin.bintray.com/kotlin-dev")
fun RepositoryHandler.ktor() = maven("https://kotlin.bintray.com/ktor")
fun RepositoryHandler.soywiz() = maven("https://dl.bintray.com/soywiz/soywiz")

/** Versions of dependencies for type-safe consistency. */
object Versions {
    const val COROUTINES = "1.2.1"
    const val KTOR = "1.2.1"
    const val SERIALIZATION = "0.11.0"
    const val LOGKAT = "0.4.5"
    const val KLOCK = "1.4.0"
}
