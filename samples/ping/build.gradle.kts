import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm")
    application
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":source"))
}

kotlin.experimental.coroutines = Coroutines.ENABLE

application {
    mainClassName = "samples.PingKt"
}