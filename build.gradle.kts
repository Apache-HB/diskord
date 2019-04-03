import com.serebit.strife.gradle.kotlinEap
import com.serebit.strife.gradle.kotlinx
import com.serebit.strife.gradle.ktor
import com.serebit.strife.gradle.soywiz

plugins {
    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.gradle.build-scan") version "2.2.1"
    kotlin("multiplatform") version "1.3.30-eap-125" apply false
    id("org.jetbrains.dokka") version "0.9.18" apply false
    id("kotlinx-serialization") version "1.3.30-eap-125" apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

buildScan {
    termsOfServiceAgree = "yes"

    publishAlwaysIf(System.getenv("PUBLISH_BUILD_SCAN") == "true")
}

subprojects {
    group = "com.serebit"
    version = "PEPE_SILVIA"

    repositories {
        jcenter()
        kotlinx()
        kotlinEap()
        ktor()
        soywiz()
    }
}
