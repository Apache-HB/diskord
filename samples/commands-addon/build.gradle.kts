plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
    implementation(project(":addons:commands"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClass.set("samples.CommandsKt")
