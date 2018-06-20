![Diskord][diskord] 

[![Discord Server][discord]](https://discord.gg/27trEwn)
[![Build Status][gitlab-ci]](https://gitlab.com/serebit/diskord/pipelines)
[![License][license]](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Donate][paypal]](https://paypal.me/gdeadshot)

Diskord is an implementation of the Discord API written entirely in Kotlin. **This project is in the beginning stages, 
and is not ready for use with bots.** If you'd like to contribute, feel free to join the Discord server using the badge 
above.

## Usage
```kotlin
fun main(args: Array<String>) {
    diskord("token") {
        onEvent { evt: MessageCreatedEvent ->
            evt.message.reply("Hello Discord!").await()
        }
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
./gradlew shadowJar
```
The compiled fatjar will be in the `build/libs` folder in the current directory after running this script.

[diskord]: https://serebit.com/assets/images/diskord-banner-nopad.svg "Diskord"
[discord]: https://discordapp.com/api/guilds/450082907185479700/widget.png?style=shield "Discord Server"
[gitlab-ci]: https://gitlab.com/serebit/diskord/badges/master/build.svg "Pipeline Status"
[license]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg "License"
[paypal]: https://img.shields.io/badge/Donate-PayPal-blue.svg "PayPal"
