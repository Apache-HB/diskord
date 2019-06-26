# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Greatly improved logging for events that could not be processed correctly. The log message now shows which event type failed to process and why the failure happened
### Changed
- Updated to Kotlin 1.3.40
- Changed `UnicodeEmoji.Companion::invoke` to a method with name `fromUnicode`
- `fromUnicode` now checks if the passed unicode is a valid unicode emoji
- Removed transitive dependency on kotlin-reflect
### Removed
- `!stop` command has been removed from the `ping` sample
### Fixed
- The process now properly exits when `BotClient::disconnect` is called
- `is` operator now works correctly with UnicodeEmoji
- Deserializing guilds no longer fails due to `joined_at` missing
- Deserializing channels no longer fails due to the ratelimit integer being greater than a max 8-bit integer
