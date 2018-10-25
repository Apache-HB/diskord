import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm") version "1.3.0-rc-198"
    application
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":source"))
}

application {
    mainClassName = "samples.PingKt"
}
