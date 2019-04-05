import com.serebit.strife.gradle.kotlinEap

rootProject.name = "strife"

include(":core", ":samples:ping", ":samples:embeds")

pluginManagement {
    repositories {
        gradlePluginPortal()
        kotlinEap()
        jcenter()
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "kotlinx-serialization") {
            useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
        }
    }
}
