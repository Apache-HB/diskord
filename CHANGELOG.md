# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Add Guild.getMember() to allow getting a member by their ID. This will attempt to get the member from Discord API if it's not found in the cache
### Changed
- Updated to Kotlin 1.3.41
- All dispatches will now wait for the guild to become available before dispatching any of its events
- Significantly improve entity data classes and how dispatches update them
- Store guild channels and DM channels separately in the cache, where the first will be stored permanently and the latter will use LruWeakCache
- Members will now be stored in LruWeakCache in GuildData instead of Map (no longer stored permanently)
- Replace Guild.owner with Guild.getOwner() as the owner is not guaranteed to be in the cache, and getting it could be suspending (bug fix)
- Replace the Java platform base-64 encoder with our own implementation

## [0.1.1] - 2019-06-30
### Added
- Greatly improved logging for events that could not be processed correctly. The log message now shows which event type failed to process and why the failure happened
### Changed
- Updated to Kotlin 1.3.40
- Changed `UnicodeEmoji.Companion::invoke` to a method with name `fromUnicode`
- `fromUnicode` now checks if the passed unicode is a valid unicode emoji
### Removed
- `!stop` command has been removed from the `ping` sample
- Removed transitive dependency on kotlin-reflect, along with any reflection usages in strife-jvm
### Fixed
- The process now properly exits when `BotClient::disconnect` is called
- `is` operator now works correctly with UnicodeEmoji
- Deserializing guilds no longer fails due to `joined_at` missing
- Deserializing channels no longer fails due to the ratelimit integer being greater than a max 8-bit integer
