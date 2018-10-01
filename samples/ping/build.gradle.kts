import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm") version "1.2.71"
    application
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":jvm"))
}

kotlin.experimental.coroutines = Coroutines.ENABLE

application {
    mainClassName = "samples.PingKt"
}
