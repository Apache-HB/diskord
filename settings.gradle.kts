rootProject.name = "diskord"
enableFeaturePreview("STABLE_PUBLISHING")

include(":common", ":jvm", ":samples:ping")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id.startsWith("kotlin-platform-") ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
