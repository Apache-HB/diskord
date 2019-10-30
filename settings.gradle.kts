rootProject.name = "strife"

include(":client", ":samples:ping", ":samples:embeds")

pluginManagement.resolutionStrategy.eachPlugin {
    val property = "plugin.${requested.id.id}"
    if (extra.has(property)) useVersion(extra.get(property) as String)
}
