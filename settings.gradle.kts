rootProject.name = "strife"

include(":client", ":samples:ping", ":samples:embeds")

pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id == "kotlinx-serialization")
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
}
