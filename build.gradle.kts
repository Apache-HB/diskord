plugins {
    id("com.github.johnrengelman.shadow") version "4.0.1"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.jfrog.bintray") version "1.8.4" apply false
    `maven-publish`
    java
}

group = "com.serebit"
version = "0.0.0"

allprojects {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://dl.bintray.com/serebit/public")
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    val sourcesJar by tasks.creating(Jar::class) {
        classifier = "sources"
        from(sourceSets["main"].allSource.sourceDirectories.files)
    }

    publishing.publications.create<MavenPublication>("BintrayRelease") {
        from(components["java"])
        artifact(sourcesJar)
        groupId = rootProject.group.toString()
        artifactId = "${rootProject.name}-${project.name}"
        version = rootProject.version.toString()
    }

    apply(from = "$rootDir/gradle/bintray-publish.gradle")
}
