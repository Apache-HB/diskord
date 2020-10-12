plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
    implementation(project(":addons:memory"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.BotMemoryKt"
