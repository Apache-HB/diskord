plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClassName = "samples.PingKt"
