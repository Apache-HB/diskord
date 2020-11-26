plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
    implementation(project(":addons:scripting"))
}

application.mainClass.set("samples.PingKt")
