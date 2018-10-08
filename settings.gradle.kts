rootProject.name = "diskord"
enableFeaturePreview("STABLE_PUBLISHING")

include(":source:common", ":source:jvm", ":samples:ping")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
