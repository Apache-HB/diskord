plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", version = "1.3.21"))
}

kotlin.sourceSets["main"].kotlin.srcDir("src")