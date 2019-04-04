import com.jfrog.bintray.gradle.BintrayExtension
import com.serebit.strife.gradle.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlinx("coroutines-core-common", version = Versions.COROUTINES))
        implementation(kotlinx("serialization-runtime-common", version = Versions.SERIALIZATION))
        implementation(ktor("client-core", version = Versions.KTOR))
        implementation(group = "com.serebit", name = "logkat-metadata", version = Versions.LOGKAT)
        api(group = "com.soywiz", name = "klock-metadata", version = Versions.KLOCK)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm().compilations["main"].defaultSourceSet.dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlinx("coroutines-core", version = Versions.COROUTINES))
        implementation(kotlinx("serialization-runtime", version = Versions.SERIALIZATION))
        implementation(ktor("client-cio", version = Versions.KTOR))
        implementation(ktor("client-websocket-jvm", version = Versions.KTOR))
        implementation(group = "com.serebit", name = "logkat-jvm", version = Versions.LOGKAT)
        api(group = "com.soywiz", name = "klock-jvm", version = Versions.KLOCK)
    }
    jvm().compilations["test"].defaultSourceSet.dependencies {
        implementation(kotlin("test-junit"))
    }

    targets.all {
        mavenPublication {
            artifactId = "${rootProject.name}-${project.name}-$targetName"
        }
    }
}

tasks.withType<KotlinCompile> {
    // configure experimental (obsolete with no alternative) coroutines channel API, along with ktor websockets
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental", "-progressive")
}

tasks.withType<Jar> {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}

tasks.dokka {
    outputDirectory = "$rootDir/public/docs"
    impliedPlatforms = mutableListOf("Common")

    // required so dokka doesn't crash on parsing multiplatform source sets, add them manually later
    kotlinTasks { emptyList() }

    sourceRoot {
        path = kotlin.sourceSets.commonMain.get().kotlin.srcDirs.single().absolutePath
        platforms = listOf("Common")
    }

    sourceRoot {
        path = kotlin.jvm().compilations["main"].defaultSourceSet.kotlin.srcDirs.single().absolutePath
        platforms = listOf("JVM")
    }
}

bintray {
    user = "serebit"
    System.getenv("BINTRAY_KEY")?.let { key = it }
    System.getenv("BINTRAY_PUBLICATION")?.let { setPublications(it) }
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "public"
        name = rootProject.name
        version.name = project.version.toString()
    })
}
