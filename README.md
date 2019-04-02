![Strife][strife-logo]

[![Discord Server][discord-guild-badge]](https://discord.gg/eYafdwP)
[![Build Status][gitlab-ci-badge]](https://gitlab.com/serebit/strife/pipelines)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Donate][paypal-badge]](https://paypal.me/gdeadshot)

---

Strife is an idiomatic Kotlin implementation of the Discord API. **This project is in the beginning stages, and is not ready for use with bots.** As such, there are no public builds available on jcenter, but there will be once more functionality has been implemented.

## Usage
```kotlin
bot("token") {
    onMessage {
        message.reply("Hello Discord!")
    }
}
```

## Another Discord library? Why bother?
Strife was created out of a desire for an idiomatic, null-safe Discord API implementation for the JVM. While several Java implementations already exist, none properly handle nulls, nor are any as easy to set up as an implementation like discord.py. Thus, Strife was born, a Kotlin implementation with the goals of proper null handling and well-structured internals.

## Roadmap
This project is fairly early in development and is still experimental. As such, there is no definitive plan or roadmap for development, although the following features are planned:

- Full integration with Discord's audio system
- Idiomatic command DSL
- First-class ports to Kotlin/Native (linux, mingw, macos)

## Build from Source
```bash
wget "https://gitlab.com/serebit/strife/-/archive/master/strife-master.tar.gz"
tar xvzf strife-master.tar.gz
cd strife-master
./gradlew build
```

## Dependencies
| Name                  | License            | Reason                       |
| --------------------- | -----------------  | ---------------------------- |
| Logkat                | Apache 2.0         | Logging                      |
| Ktor                  | Apache 2.0         | HTTP requests and websockets |
| kotlinx.coroutines    | Apache 2.0         | Parallelism                  |
| kotlinx.serialization | Apache 2.0         | Parsing/encoding JSON        |
| Klock                 | MIT and Apache 2.0 | Date and time                |

See `source/build.gradle.kts` for the exhaustive list of dependencies.

[strife-logo]: https://serebit.com/images/strife-banner-nopad.svg "Strife"
[discord-guild-badge]: https://discordapp.com/api/guilds/450082907185479700/widget.png?style=shield "Discord Server"
[gitlab-ci-badge]: https://gitlab.com/serebit/strife/badges/master/build.svg "Pipeline Status"
[license-badge]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg "License"
[paypal-badge]: https://img.shields.io/badge/Donate-PayPal-blue.svg "PayPal"
