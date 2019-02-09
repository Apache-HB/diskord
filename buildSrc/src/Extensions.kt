package com.serebit.strife.gradle

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"
fun KotlinDependencyHandler.api(group: String, name: String, version: String) = api("$group:$name:$version")
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

fun RepositoryHandler.kotlinx() = maven("https://dl.bintray.com/kotlin/kotlinx")
fun RepositoryHandler.kotlinEap() = maven("https://dl.bintray.com/kotlin/kotlin-eap")

object Versions {
    const val coroutines = "1.1.1"
    const val ktor = "1.1.2"
    const val serialization = "0.10.0"
    const val http4k = "3.112.1"
    const val logkat = "0.4.2"
}
