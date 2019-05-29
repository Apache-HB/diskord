rootProject.name = "strife"

include(
    ":core", ":commands", 
    ":samples:ping", ":samples:embeds", ":samples:commands-feature"
)

pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id == "kotlinx-serialization")
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
}
