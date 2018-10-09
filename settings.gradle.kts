rootProject.name = "diskord"
enableFeaturePreview("STABLE_PUBLISHING")

include(":source", ":samples:ping")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
