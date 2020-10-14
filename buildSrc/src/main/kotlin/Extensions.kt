package com.serebit.strife.buildsrc

import groovy.util.Node
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

private const val projectName = "strife"
private const val projectDescription = "An idiomatic Kotlin implementation of the Discord API"
private val projectDevelopers = listOf("serebit", "JonoAugustine", "legendoflelouch")

fun KotlinDependencyHandler.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"

fun KotlinDependencyHandler.api(group: String, name: String, version: String) =
    api("$group:$name:$version")

fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

val Project.fullPath get() = "${rootProject.name}${project.path.replace(":", "-")}"
fun Project.jarTask() = tasks.registering(Jar::class) {
    archiveClassifier.set(name.removeSuffix("Jar"))
}

fun PublishingExtension.createMavenRepositories() {
    // create public
    repositories.maven("https://api.bintray.com/maven/serebit/public/$projectName/;publish=0;override=1") {
        name = "public"
        credentials {
            username = "serebit"
            System.getenv("BINTRAY_KEY")?.let { password = it }
        }
    }

    // create snapshot
    repositories.maven("https://gitlab.com/api/v4/projects/16096337/packages/maven") {
        name = "snapshot"
        credentials(HttpHeaderCredentials::class) {
            name = "Job-Token"
            System.getenv("CI_JOB_TOKEN")?.let { value = it }
        }

        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

private fun Node.add(key: String, value: String) = appendNode(key).setValue(value)
private inline fun Node.node(key: String, content: Node.() -> Unit) = appendNode(key).also(content)

fun MavenPublication.configureForMavenCentral(javadocJar: TaskProvider<Jar>, sourcesJar: TaskProvider<Jar>) {
    artifact(javadocJar)
    if (name == "kotlinMultiplatform") artifact(sourcesJar)

    pom.withXml {
        asNode().run {
            add("name", projectName)
            add("description", projectDescription)
            add("url", "https://gitlab.com/serebit/$projectName")
            node("organization") {
                add("name", "com.serebit")
                add("url", "https://serebit.com")
            }
            node("issueManagement") {
                add("system", "gitlab")
                add("url", "https://gitlab.com/serebit/$projectName/issues")
            }
            node("licenses") {
                node("license") {
                    add("name", "Apache License 2.0")
                    add("url", "https://gitlab.com/serebit/$projectName/blob/master/LICENSE.md")
                    add("distribution", "repo")
                }
            }
            node("scm") {
                add("url", "https://gitlab.com/serebit/$projectName")
                add("connection", "scm:git:git://gitlab.com/serebit/$projectName.git")
                add("developerConnection", "scm:git:ssh://gitlab.com/serebit/$projectName.git")
            }
            node("developers") {
                projectDevelopers.forEach {
                    node("developer") {
                        add("name", it)
                    }
                }
            }
        }
    }
}
