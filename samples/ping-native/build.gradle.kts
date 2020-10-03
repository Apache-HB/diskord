plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(project(":client"))
    }

    linuxX64().binaries.executable {
        entryPoint = "samples.main"
    }
}
