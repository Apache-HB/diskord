plugins {
    kotlin("jvm") version "1.3.20"
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.PingKt"
