rootProject.name = "diskord"
enableFeaturePreview("STABLE_PUBLISHING")

include(":source", ":samples:ping")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("http://dl.bintray.com/kotlin/kotlin-eap")
    }
}
