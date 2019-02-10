import com.serebit.strife.gradle.kotlinEap

rootProject.name = "strife"

include(":core", ":samples:ping")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://jitpack.io")
        kotlinEap()
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "kotlinx-serialization") {
            useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
        }
    }
}
