import com.serebit.strife.gradle.kotlinEap

rootProject.name = "strife"

include(":core", ":commands", ":samples:ping", ":samples:embeds", ":samples:modules")

pluginManagement {
    repositories {
        gradlePluginPortal()
        kotlinEap()
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "kotlinx-serialization") {
            useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
        }
    }
}
