package com.serebit.strife.buildsrc

import groovy.util.Node
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.maven
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.kotlinx(module: String, version: String): String = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun KotlinDependencyHandler.ktor(module: String, version: String): String = "io.ktor:ktor-$module:$version"
fun KotlinDependencyHandler.api(group: String, name: String, version: String): Dependency? = api("$group:$name:$version")
fun KotlinDependencyHandler.implementation(group: String, name: String, version: String): Dependency? =
    implementation("$group:$name:$version")

fun RepositoryHandler.kotlinx(): MavenArtifactRepository = maven("https://kotlin.bintray.com/kotlinx")
fun RepositoryHandler.kotlinEap(): MavenArtifactRepository = maven("https://kotlin.bintray.com/kotlin-eap")

val Project.fullPath: String get() = "${ProjectInfo.name}${project.path.replace(":", "-")}"

fun PublishingExtension.createBintrayRepository(accessKey: String?): MavenArtifactRepository =
    repositories.maven("https://api.bintray.com/maven/serebit/public/${ProjectInfo.name}/;publish=0") {
        name = "bintray"

        credentials {
            username = "serebit"
            accessKey?.let { password = it }
        }
    }

private fun Node.add(key: String, value: String) = appendNode(key).setValue(value)
private inline fun Node.node(key: String, content: Node.() -> Unit) = appendNode(key).also(content)

fun MavenPublication.configureForMavenCentral(javadocJar: Jar, sourcesJar: Jar) {
    artifact(javadocJar)
    if (name == "kotlinMultiplatform") artifact(sourcesJar)

    pom.withXml {
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
