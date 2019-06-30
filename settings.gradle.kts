rootProject.name = "strife"

include(":client", ":samples:ping", ":samples:embeds")

enableFeaturePreview("GRADLE_METADATA")

pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id == "kotlinx-serialization")
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
}
