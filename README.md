![Diskord][diskord-logo]

[![Discord Server][discord-guild-badge]](https://discord.gg/27trEwn)
[![Build Status][gitlab-ci-badge]](https://gitlab.com/serebit/diskord/pipelines)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Donate][paypal-badge]](https://paypal.me/gdeadshot)

---

Diskord is an implementation of the Discord API written entirely in Kotlin. **This project is in the beginning stages, 
and is not ready for use with bots.** If you'd like to contribute, feel free to join the Discord server using the badge 
above.

## Usage
```kotlin
bot("token") {
    onEvent { evt: MessageCreatedEvent ->
        evt.message.reply("Hello Discord!")
    }
}
```

## Another Discord library? Why bother?
Diskord was created out of a desire for an idiomatic, null-safe Discord API implementation for the JVM. While several
 Java implementations already exist, none properly handle nulls, nor are any as easy to set up as an implementation like
 discord.py. Thus, Diskord was born, a Kotlin implementation with the goals of proper null handling and well-structured
 internals.
 
## Build from Source
```bash
wget "https://gitlab.com/serebit/diskord/-/archive/master/diskord-master.tar.gz"
tar xvzf diskord-master.tar.gz
cd diskord-master
./gradlew build
```
The compiled jar will be in the `source/build/libs` folder in the current directory after running this script.

## Roadmap
This project is fairly early in development and is still experimental. As such, there is no definitive plan or 
roadmap for development, although the following features are planned:

- Full integration with Discord's audio system
- First-class ports to Kotlin/Native (linux, mingw, macos)

## Dependencies
| Name    | License    | Reason                |
| ------- | ---------- |---------------------- |
| Logkat  | Apache 2.0 | Logging events        |
| Ktor    | Apache 2.0 | HTTP requests         |
| Http4k  | Apache 2.0 | Websockets            |
| Jackson | Apache 2.0 | Parsing/encoding JSON |

See `source/build.gradle.kts` for the exhaustive list of dependencies.

[diskord-logo]: https://serebit.com/images/diskord-banner-nopad.svg "Diskord"
[discord-guild-badge]: https://discordapp.com/api/guilds/450082907185479700/widget.png?style=shield "Discord Server"
[gitlab-ci-badge]: https://gitlab.com/serebit/diskord/badges/master/build.svg "Pipeline Status"
[license-badge]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg "License"
[paypal-badge]: https://img.shields.io/badge/Donate-PayPal-blue.svg "PayPal"
