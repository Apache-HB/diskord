import com.serebit.strife.gradle.kotlinEap
import com.serebit.strife.gradle.kotlinx
import com.serebit.strife.gradle.soywiz

plugins {
    id("com.github.ben-manes.versions") version "0.20.0"
    kotlin("multiplatform") version "1.3.21" apply false
    id("kotlinx-serialization") version "1.3.21" apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

allprojects {
    group = "com.serebit"
    version = "0.0.0"

    repositories {
        jcenter()
        kotlinx()
        kotlinEap()
        soywiz()
        maven("https://dl.bintray.com/kotlin/kotlinx")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://jitpack.io")
    }
}
