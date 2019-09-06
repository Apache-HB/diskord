package com.serebit.strife.gradle

import groovy.util.Node
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.maven
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"
fun KotlinDependencyHandler.api(group: String, name: String, version: String) = api("$group:$name:$version")
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String) =
    implementation("$group:$name:$version")

fun RepositoryHandler.kotlinx() = maven("https://kotlin.bintray.com/kotlinx")
fun RepositoryHandler.kotlinEap() = maven("https://kotlin.bintray.com/kotlin-eap")

val Project.fullPath get() = "${ProjectInfo.name}${project.path.replace(":", "-")}"

fun PublishingExtension.createBintrayRepository(accessKey: String?) =
    repositories.maven("https://api.bintray.com/maven/serebit/public/${ProjectInfo.name}/;publish=0") {
        name = "bintray"

        credentials {
            username = "serebit"
            accessKey?.let { password = it }
        }
    }

fun MavenPublication.configureForMavenCentral(javadocJar: Jar, sourcesJar: Jar) {
    artifact(javadocJar)
    if (name == "kotlinMultiplatform") artifact(sourcesJar)

    pom.withXml {
        fun Node.add(key: String, value: String) {
            appendNode(key).setValue(value)
        }

        fun Node.node(key: String, content: Node.() -> Unit) {
            appendNode(key).also(content)
        }

        asNode().run {
            add("description", ProjectInfo.description)
            add("name", ProjectInfo.name)
            add("url", "https://gitlab.com/serebit/${ProjectInfo.name}")
            node("organization") {
                add("name", "com.serebit")
                add("url", "https://serebit.com")
            }
            node("issueManagement") {
                add("system", "gitlab")
                add("url", "https://gitlab.com/serebit/${ProjectInfo.name}/issues")
            }
            node("licenses") {
                node("license") {
                    add("name", "Apache License 2.0")
                    add("url", "https://gitlab.com/serebit/${ProjectInfo.name}/blob/master/LICENSE.md")
                    add("distribution", "repo")
                }
            }
            node("scm") {
                add("url", "https://gitlab.com/serebit/${ProjectInfo.name}")
                add("connection", "scm:git:git://gitlab.com/serebit/${ProjectInfo.name}.git")
                add("developerConnection", "scm:git:ssh://gitlab.com/serebit/${ProjectInfo.name}.git")
            }
            node("developers") {
                node("developer") {
                    add("name", "serebit")
                }
                node("developer") {
                    add("name", "legendoflelouch")
                }
                node("developer") {
                    add("name", "JonoAugustine")
                }
            }
        }
    }
}
