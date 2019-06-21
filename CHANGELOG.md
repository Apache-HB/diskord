# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Updated to Kotlin 1.3.40
### Removed
- `!stop` command has been removed from the `ping` sample
### Fixed
- The process now properly exits when `BotClient::disconnect` is called
- Removed transitive dependency on kotlin-reflect
