plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":client"))
    implementation(project(":scripting"))
}

application.mainClassName = "samples.PingKt"
