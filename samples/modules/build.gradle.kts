plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.ModulesKt"
