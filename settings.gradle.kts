rootProject.name = "diskord"
enableFeaturePreview("STABLE_PUBLISHING")

pluginManagement {
    repositories {
        maven("https://kotlin.bintray.com/kotlinx")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("kotlinx")) {
                val name = requested.id.id.substringAfterLast('-')
                useModule("org.jetbrains.kotlinx:kotlinx-gradle-$name-plugin:${requested.version}")
            }
        }
    }
}
