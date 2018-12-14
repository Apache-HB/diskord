plugins {
    id("com.github.ben-manes.versions") version "0.20.0"
}

group = "com.serebit"
version = "0.0.0"

allprojects {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlinx")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
