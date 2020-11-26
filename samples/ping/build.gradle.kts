plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")

application.mainClass.set("samples.PingKt")
