rootProject.name = "strife"
enableFeaturePreview("GRADLE_METADATA")

include(":core", ":samples:ping")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "kotlinx-serialization") {
            useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
        }
    }
}
