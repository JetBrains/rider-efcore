# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [231.0.0-eap01] - 2023-01-19
### Added
- Enable support for Rider 2023.1 EAP 1

## [223.3.1] - 2023-01-23
### Fixed
- DbContext is not persisted between dialogs
- EF Core Quick Actions popup is empty
- Exception when trying to call Drop Database dialog submit button
- Project-specific saved values are not restored when project in dropdown changes

## [223.3.0] - 2023-01-10
### Changed
- Use .NET CLI from Rider's settings instead of global "dotnet" one

### Fixed
- Exception thrown when dotnet not installed or accessible bug
- Transitively available DbContexts in Migrations project are not visible in the dropdown 

## [223.2.0] - 2022-12-15
### Changed
- Target migration in Update database dialog is now represented as a drop-down list instead of text input field (#130 by @unopcpavilion)

### Fixed
- Target migration field selected oldest migration instead of newest as preselected value 

## [223.1.0] - 2022-12-07
### Added
- Most common values in dialogs are saved between instances 
- Ability to run commands in terminal-like tool window
- Support for a stable Rider 2022.3

### Fixed
- DbContext class not showing up (#105)

## [223.0.0] - 2022-09-30
### Added
- Enable support for Rider 2022.3 EAP

## [222.2.0] - 2022-09-30
### Added
- F.A.Q. hyperlink near Startup project dropdown (#115 by @TheBottleCyber)

### Fixed
- Abstract DbContexts are visible in DbContext dorpdown (#109 by @vova-lantsov-dev)
- Connection string is escaped improperly (#110 by @vova-lantsov-dev)
- Startup exception when dotnet tools cache is not yet initialized
- `NullRef` on invalid migration classes instead of ignoring

## [222.1.1] - 2022-08-15
### Fixed
- Project structure synchronization issues
- SQL script generation dialog is missing '0' case for "From migration" dropdown
- `dotnet tool install` command failure on unauthorized feeds

## [222.1.0] - 2022-08-08
### Added
- Support for .NET Standard 2.0 projects (Migrations projects only) (#101 by @Sander0542)

### Fixed
- Exception when trying to install EF Core tools from plugin
- Data inconsistency in projects dropdowns after changing solution structure

## [222.0.0] - 2022-08-02
### Added
- Support for `dotnet ef` as local tool
- Ability to preview commands before executing
- Ability to pass additional command line arguments for Startup project
- SQL script generation for given migrations range

### Changed
- Change version numbering to reflect Rider version in it

### Fixed
- Incorrect behaviour of EF Core tools version retrieval

## [1.4.1] - 2022-06-16
### Fixed
- Rider 2022.2 EAP3 compatibility issue

## [1.4.0] - 2022-05-05
### Added
- Support for .NET 7 projects (#78 by @Maruf61)

### Fixed
- RPC timeout when trying to load startup projects on Rider startup
- Projects with platform specific target frameworks not presented in dropdowns

## [1.3.1] - 2022-04-19
### Added
- Enable support for Rider 2022.1

## [1.3.0] - 2022-03-28
### Added
- Ability to open EF Core action under `Tools` application menu entry
- EF Core Quick Actions window
- Removal of migration parent folder after Remove Last Migration if there are no migrations (#51 by @kolosovpetro)

### Changed
- Improve Startup project detection logic (#59 by @kolosovpetro)
- Show only migrations related to selected DbContext in Update Database's Target migration autocompletion (#50 by @kolosovpetro)

## [1.2.1] - 2022-01-24
### Fixed
- NoSuchMethodError using the new DSL v2
- Disabled optional fields require validation

## [1.2.0] - 2022-01-24
### Added
- DbContext Scaffolding
- Output folder for Add Migration dialog (#44 by @kolosovpetro)

### Changed
- Make Target framework optional (#41 by @kolosovpetro)

## [1.1.3] - 2022-01-13
### Fixed
- Unable to run any action under Mac OS X

## [1.1.2] - 2022-01-12
### Fixed
- Duplicated items in dropdowns when projects has multiple target framework
- Duplicated items in the build configuration dropdown when solution has multiple target platforms
- Exceptions when trying to open any action with no build configuration in solution

## [1.1.1] - 2021-12-09
### Fixed
- Projects with whitespaces in project file path are recognized incorrectly
- Default Update database target migration is selected incorrectly

## [1.1.0] - 2021-12-08
### Added
- General: Support .NET Core 3.1 and .NET Standard 2.1 projects (#30 by @kolosovpetro)
- Upgrade to Rider 2021.3 in stable channel

## [1.0.0] - 2021-12-05
### Added
- Creating migrations
- Removing last created migration
- Persisting selected migrations and startup projects between dialogs
- Suggesting installing dotnet `ef command` line tools if not installed (when opening solution that contains EF Core related projects)
- Deleting used database

[Unreleased]: https://github.com/seclerp/rider-efcore/compare/v231.0.0-eap01...HEAD
[231.0.0-eap01]: https://github.com/seclerp/rider-efcore/compare/v223.3.1...v231.0.0-eap01
[223.3.1]: https://github.com/seclerp/rider-efcore/compare/v223.3.0...v223.3.1
[223.3.0]: https://github.com/seclerp/rider-efcore/compare/v223.2.0...v223.3.0
[223.2.0]: https://github.com/seclerp/rider-efcore/compare/v223.1.0...v223.2.0
[223.1.0]: https://github.com/seclerp/rider-efcore/compare/v223.0.0...v223.1.0
[223.0.0]: https://github.com/seclerp/rider-efcore/compare/v222.2.0...v223.0.0
[222.2.0]: https://github.com/seclerp/rider-efcore/compare/v222.1.1...v222.2.0
[222.1.1]: https://github.com/seclerp/rider-efcore/compare/v222.1.0...v222.1.1
[222.1.0]: https://github.com/seclerp/rider-efcore/compare/v222.0.0...v222.1.0
[222.0.0]: https://github.com/seclerp/rider-efcore/compare/v1.4.1-eap...v222.0.0
[1.4.1]: https://github.com/seclerp/rider-efcore/compare/v1.4.0...v1.4.1-eap
[1.4.0]: https://github.com/seclerp/rider-efcore/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/seclerp/rider-efcore/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/seclerp/rider-efcore/compare/v1.2.1...v1.3.0
[1.2.1]: https://github.com/seclerp/rider-efcore/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/seclerp/rider-efcore/compare/v1.1.3...v1.2.0
[1.1.3]: https://github.com/seclerp/rider-efcore/compare/v1.1.2...v1.1.3
[1.1.2]: https://github.com/seclerp/rider-efcore/compare/v1.1.1...v1.1.2
[1.1.1]: https://github.com/seclerp/rider-efcore/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/seclerp/rider-efcore/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/seclerp/rider-efcore/releases/tag/v1.0.0