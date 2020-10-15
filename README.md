![Strife][strife-logo]

[![Discord Server][discord-guild-badge]](https://discord.gg/eYafdwP)
[![Download][bintray-badge]](https://bintray.com/serebit/public/strife)
[![Build Status][gitlab-ci-badge]](https://gitlab.com/serebit/strife/pipelines)
[![Documentation][kdoc-badge]](https://serebit.gitlab.io/strife/docs/client)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Ko-fi][kofi-badge]](https://ko-fi.com/serebit)

---

Strife is an idiomatic Kotlin implementation of the Discord API. **This project does not yet implement the entire
Discord feature set.** If you'd still like to try it out, check out the Get Started section below.

## Usage

```kotlin
bot("token") {
    onMessageCreate {
        message.reply("Hello Discord!")
    }
}
```

You can see more samples in the `samples` directory and the [wiki](https://gitlab.com/serebit/strife/-/wikis/home).

## Get Started

The easiest way to get started is by running the following:

```
git clone https://gitlab.com/serebit/strife-quickstart
```

This creates a local copy of a simple and runnable implementation of Strife. It's licensed via the Unlicense, so you can
use the code for whatever you want and modify it however you want.

If you want to try Strife with an existing Discord bot, you'll have to add the following to your `build.gradle.kts` to
get started:

```kotlin
dependencies {
    implementation("com.serebit.strife", "strife-client-jvm", "0.4.0")
}
```

### Snapshot Builds

If you'd rather live on the edge, Strife auto-publishes builds for every commit to the master branch to Bintray in a
separate repository. These builds are not published to Bintray,
so `https://gitlab.com/api/v4/projects/6502506/packages/maven` needs to be added to your Gradle repositories for these
versions to be resolved:

```kotlin
repositories {
    maven("https://gitlab.com/api/v4/projects/6502506/packages/maven")
}
```

Snapshot builds use a version number with the snapshot's short commit hash, e.g. `dev-b1afb0be`. You can find the latest
snapshot versions [here](https://gitlab.com/serebit/strife/-/packages).

## Another Discord library? Why bother?

Good question! And the answer is, because I have issues with most of them. I wanted a Kotlin implementation that solved
all the problems in other libraries, and I figured who better to make one than myself and likeminded Kotlin developers?
Once other people got on board, I realized my idea would actually pan out, and now we're here. It hasn't been easy, but
we're forging ahead until we get it done!

## Roadmap

This project is fairly early in development and is still experimental. As such, there is no definitive plan or roadmap
for development, although the following features are planned:

- Full integration with Discord's audio system
- First-class ports to Kotlin/Native (linux first, mingw later, possibly more after that)

## Build from Source

```bash
wget "https://gitlab.com/serebit/strife/-/archive/master/strife-master.tar.gz"
tar xvzf strife-master.tar.gz
cd strife-master
./gradlew build
```

This builds all modules, including samples. If you want to build only one module, prefix the `build` task with the name
of the module, like so: `./gradlew :core:build`. To see the full list of tasks, run `./gradlew tasks`. To publish the
library to Maven's local repository, run the following:

```bash
./gradlew publishToMavenLocal
```

## Dependencies

| Name                  | License            | Reason                       |
| --------------------- | -----------------  | ---------------------------- |
| Logkat                | Apache 2.0         | Logging                      |
| Ktor                  | Apache 2.0         | HTTP requests and websockets |
| kotlinx.coroutines    | Apache 2.0         | Concurrency                  |
| kotlinx.serialization | Apache 2.0         | Parsing/encoding JSON        |

The exhaustive list of dependencies can be found in each module's `build.gradle.kts` file.

## Developers

<a href="https://gitlab.com/serebit"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/1184009/avatar.png"></a>
<a href="https://gitlab.com/JonoAugustine"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/3489815/avatar.png"></a>
<a href="https://gitlab.com/legendoflelouch"><img width="96" src="https://assets.gitlab-static.net/uploads/-/system/user/avatar/3653603/avatar.png"></a>

[strife-logo]: https://serebit.com/images/strife-banner-nopad.svg "Strife"

[discord-guild-badge]: https://discordapp.com/api/guilds/450082907185479700/widget.png?style=shield "Discord Server"

[bintray-badge]: https://api.bintray.com/packages/serebit/public/strife/images/download.svg "Download"

[gitlab-ci-badge]: https://gitlab.com/serebit/strife/badges/master/pipeline.svg "Pipeline Status"

[kdoc-badge]: https://img.shields.io/badge/docs-kdoc-informational.svg "Documentation"

[license-badge]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg "License"

[kofi-badge]: https://img.shields.io/badge/-ko--fi-ff5f5f?logo=ko-fi&logoColor=white "Ko-fi"
