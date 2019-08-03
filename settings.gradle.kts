rootProject.name = "strife"

include(
    ":client", ":commands",
    ":samples:ping", ":samples:embeds", ":samples:commands-feature"
)

enableFeaturePreview("GRADLE_METADATA")

pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id == "kotlinx-serialization")
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
}
