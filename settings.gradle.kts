import com.serebit.strife.gradle.ProjectInfo

rootProject.name = ProjectInfo.name

include(":client", ":samples:ping", ":samples:embeds")

enableFeaturePreview("GRADLE_METADATA")

pluginManagement.resolutionStrategy.eachPlugin {
    when(requested.id.id) {
        "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
        "kotlinx-atomicfu" -> useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
    }
}
