plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":client"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.MessageEmbedKt"
