![Strife][strife-logo]

[![Discord Server][discord-guild-badge]](https://discord.gg/eYafdwP)
[![Download][bintray-badge]](https://bintray.com/serebit/public/strife)
[![Build Status][gitlab-ci-badge]](https://gitlab.com/serebit/strife/pipelines)
[![Documentation][kdoc-badge]](https://serebit.gitlab.io/strife/docs/client)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0.html)

---

Strife is an idiomatic Kotlin implementation of the Discord API. **This project is in the beginning stages, and does not implement the entire Discord feature set.** If you'd still like to try it out, check out the Get Started section below.

## Usage
```kotlin
bot("token") {
    onMessage {
        message.reply("Hello Discord!")
    }
}
```

You can see more samples in the `samples` directory.

## Get Started
The easiest way to get started is by running the following:
```
git clone https://gitlab.com/serebit/strife-quickstart
```
This creates a local copy of a simple and runnable implementation of Strife. It's licensed via the Unlicense, so you can use the code for whatever you want and modify it however you want.

If you want to try Strife with an existing Discord bot, you'll have to add the following to your `build.gradle.kts` to get started:
```kotlin
repositories {
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(group = "com.serebit.strife", name = "strife-client-jvm", version = "0.2.0")
}
```

## Another Discord library? Why bother?
Good question! And the answer is, because I have issues with most of them. I wanted a Kotlin implementation that solved all the problems in other libraries, and I figured who better to make one than myself and likeminded Kotlin developers? Once other people got on board, I realized my idea would actually pan out, and now we're here. It hasn't been easy, but we're forging ahead until we get it done!

## Roadmap
This project is fairly early in development and is still experimental. As such, there is no definitive plan or roadmap for development, although the following features are planned:

- Full integration with Discord's audio system
- Idiomatic command DSL
- First-class ports to Kotlin/Native (linux and mingw)
- Scripting support for JVM
- Module system, similar to discord.py's "cogs"

## Build from Source
```bash
wget "https://gitlab.com/serebit/strife/-/archive/master/strife-master.tar.gz"
tar xvzf strife-master.tar.gz
cd strife-master
./gradlew build
```

This builds all modules, including samples. If you want to build only one module, prefix the `build` task with the name of the module, like so: `./gradlew :core:build`. To see the full list of tasks, run `./gradlew tasks`. To publish the library to Maven's local repository, run the following:
```bash
./gradlew publishToMavenLocal
```

You can then add the published libraries to your project's dependencies. However, you will have to add the following repositories to your project's buildscript for this to work:
```kotlin
jcenter()
mavenLocal()
maven("https://kotlin.bintray.com/kotlinx")
```

## Dependencies
| Name                  | License            | Reason                       |
| --------------------- | -----------------  | ---------------------------- |
| Logkat                | Apache 2.0         | Logging                      |
| Ktor                  | Apache 2.0         | HTTP requests and websockets |
| kotlinx.coroutines    | Apache 2.0         | Concurrency                  |
| kotlinx.serialization | Apache 2.0         | Parsing/encoding JSON        |
| Klock                 | MIT and Apache 2.0 | Date and time                |

The exhaustive list of dependencies can be found in each module's `build.gradle.kts` file. Dependency versions are located in `buildSrc/src/Extensions.kt`.

## Developers
<a href="https://gitlab.com/serebit"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/1184009/avatar.png"></a>
<a href="https://gitlab.com/JonoAugustine"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/3489815/avatar.png"></a>
<a href="https://gitlab.com/legendoflelouch"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/3653603/avatar.png"></a>

[strife-logo]: https://serebit.com/images/strife-banner-nopad.svg "Strife"
[discord-guild-badge]: https://discordapp.com/api/guilds/450082907185479700/widget.png?style=shield "Discord Server"
[bintray-badge]: https://api.bintray.com/packages/serebit/public/strife/images/download.svg "Download"
[gitlab-ci-badge]: https://gitlab.com/serebit/strife/badges/master/build.svg "Pipeline Status"
[kdoc-badge]: https://img.shields.io/badge/docs-kdoc-informational.svg "Documentation"
[license-badge]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg "License"
