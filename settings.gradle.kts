import com.serebit.strife.buildsrc.ProjectInfo

rootProject.name = ProjectInfo.name

include(
    ":client", ":commands",
    ":samples:ping", ":samples:embeds", ":samples:commands-feature"
)

enableFeaturePreview("GRADLE_METADATA")

pluginManagement.resolutionStrategy.eachPlugin {
    val property = "plugin.${requested.id.id}"
    if (extra.has(property)) useVersion(extra.get(property) as String)
}
