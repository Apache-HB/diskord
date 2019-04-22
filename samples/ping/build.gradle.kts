plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
    implementation(project(":commands"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.PingKt"
