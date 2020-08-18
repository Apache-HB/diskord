rootProject.name = "strife"

include(":client", ":samples:ping", ":samples:embeds")

pluginManagement.repositories {
    gradlePluginPortal()
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
}
