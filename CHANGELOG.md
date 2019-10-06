# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

#### Added
- [#27](https://gitlab.com/serebit/strife/issues/27) Full support for Webhook API (support for standalone webhooks will be added in a later release)
    - Add `BotClient.getWebhook()` function
    - Add `Guild.getWebhooks()` function
    - Add `GuildMessageChannel.getWebhooks()` function
    - Add `GuildMessageChannel.createWebhook()` function
    - Create `Webhook` entity

## 0.3.0 (2019-10-04)

#### Added 
- More shorthand BotBuilder extensions such as `onChannelCreate`, `onMessageEdit`, etc
- Releases are now mirrored to Maven Central
- Snapshot builds of each commit to master are now published to a separate Bintray repository
- `GuildRole` now has a `guildId` property, alongside `getGuild` and `delete` functions
- Add enum `Type` to `Channel` interface
- Add `Guild#getSelfMember` extension function
- Add `GuildBan` class and `Guild#getBans` function
- Add `GuildRole#compareTo` function to compare GuildRole positions
- Add common interface `GuildMessageChannel` for `GuildTextChannel` and `GuildNewsChannel`
- [#4](https://gitlab.com/serebit/strife/issues/4) Implement all remaining event types
- [#17](https://gitlab.com/serebit/strife/issues/17) Add global markdown extensions on String
- [#20](https://gitlab.com/serebit/strife/issues/20) Add terminable event listeners, which are removed from the client when their task is successful
- [#7](https://gitlab.com/serebit/strife/issues/7) Implement all remaining Routes and large portion of related API
    - Guild
        - createRole
        - get & delete invites
        - get, create, & delete Integrations
        - prune and getPruneCount
        - unBan
        - getGuildEmbed
        - getVanityUrl
    - GuildMember
        - setNickname
        - addRole (by ID or by GuildRole)
        - removeRole (by ID or by GuildRole) 
        - setDeafen, deafen, un-deafen
        - setMuted, mute, unmute
        - move (by ID or voice chanel)
    - GuildRole
        - setName
        - setPermissions, removePermission, addPermission
        - setColor
        - setHoisted, hoist, un-hoist
        - setMentionable
        - Set position, raise, lower
    - GuildChannel
        - get & create Invites
    - GuildIntegration (new)
        - setExpireBehavior
        - setGracePeriod
        - setEmojiEnabled
        - sync
        - delete
    - GuildEmbed (new)
        - setChannel (by ID or GuildChannel)
        - setEnabled, enable, disable
    - Invite (new)
        - delete

#### Changed 
- Explicit typing on all public API
- Rename some events for consistency
- Rename some BotBuilder extensions for consistency

#### Fixed
- [#18](https://gitlab.com/serebit/strife/issues/18) Event listeners with interface types now trigger when a subtype of that event type is received
- Listeners added to a `BotBuilder` after the `build()` function is called no longer apply to the previously-built `BotClient`

## 0.2.1 (2019-09-03)

#### Changed 
- Updated stack to Kotlin 1.3.50

#### Fixed 
- Doubled the speed of encoding bytes to base64

## 0.2.0 (2019-08-12)

#### Added 
- Add a new `Presence` class to support all presence properties
- Activity now supports showing thumbnails for Spotify songs
- Implement `GuildRoleCreateEvent`, `GuildRoleUpdateEvent`, `GuildRoleDeleteEvent`, `GuildIntegrationsUpdateEvent` and `GuildMembersChunkEvent`
- Add `presence` property to `GuildMember`
- Add `getChannel`, `getTextChannel`, `getVoiceChannel`, and `getRole` functions to `Guild`
- Add `presences` property to `Guild`

#### Changed 
- Optimizations for Color math
- Improve existing `Activity` class and integrate it into `Presence`
- `BotClient.updatePresence()` no longer uses `Activity`, instead it uses `Pair<Activity.Type, String>`
- `PresenceUpdateEvent` now provides the full `Presence` object instead of only `Activity` and `OnlineStatus`
- `PresenceUpdateEvent` now implements `GuildEvent`

#### Fixed 
- Fix roles and channels not being updated in Guild
- Fix memory leaking when sending HTTP requests

## 0.1.2 (2019-08-02)

#### Added 
- Add Guild.getMember() to allow getting a member by their ID. This will attempt to get the member from Discord API if it's not found in the cache

#### Changed 
- Updated to Kotlin 1.3.41
- All guild dispatches will now wait for the guild to become available before being dispatched
- Improved the internal representation of entity classes and how events update them
- Improved speed and memory usage of channel deserialization
- Guild channels are now cached permanently
- Replace the Java platform base-64 encoder with our own implementation
- Replace Guild.owner with Guild.getOwner(), in case the owner member is no longer in the cache
- Members are no longer be cached permanently, and will be removed from the cache if going unused

### Fixed 
- [#14](https://gitlab.com/serebit/strife/issues/14) Add Watching activity type

## 0.1.1 (2019-06-30)

#### Added 
- Greatly improved logging for events that could not be processed correctly. The log message now shows which event type failed to process and why the failure happened

#### Changed 
- Updated to Kotlin 1.3.40
- Changed `UnicodeEmoji.Companion::invoke` to a method with name `fromUnicode`
- `fromUnicode` now checks if the passed unicode is a valid unicode emoji

#### Removed 
- `!stop` command has been removed from the `ping` sample
- Removed transitive dependency on kotlin-reflect, along with any reflection usages in strife-jvm

#### Fixed 
- The process now properly exits when `BotClient::disconnect` is called
- `is` operator now works correctly with UnicodeEmoji
- Deserializing guilds no longer fails due to `joined_at` missing
- Deserializing channels no longer fails due to the ratelimit integer being greater than a max 8-bit integer
