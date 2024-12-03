<div align="center">
  <img alt="Logo" src="./img/logo.svg#gh-light-mode-only" width="100">
  <img alt="Logo" src="./img/logo-dark.svg#gh-dark-mode-only" width="100">
  <h2>Entity Framework Core UI plugin for JetBrains Rider</h2>

  This plugin introduces Entity Framework Core commands' UI inside JetBrains Rider.

  <a href="https://github.com/JetBrains"><img src="https://img.shields.io/badge/JetBrains-official-orange?logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyBoZWlnaHQ9IjMyLjAwMDAxIiB2aWV3Qm94PSIwIDAgMzIgMzIuMDAwMDEiIHdpZHRoPSIzMiIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBkPSJtMCAwaDMydjMyLjAwMDAxaC0zMnoiLz48cGF0aCBkPSJtNCAyNi4wMDAwMWgxMnYyaC0xMnoiIGZpbGw9IiNmZmYiLz48L3N2Zz4%3D" alt="JetBrains Official"></a>
  <a href="https://github.com/seclerp/rider-efcore/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/JetBrains/rider-efcore/build.yml?logo=github" alt="Build"></a>
</div>

---

### Features

There are a couple of UI stuff for:
- Creating migrations
- Updating database with selected migration (including migration names autocompletion)
- DbContext scaffolding from existing database
- Creating SQL scripts from the migration range
- Removing last created migration
- Suggesting installing `dotnet ef` command line tools if not installed (when opening solution that contains EF Core related projects)
- Previewing commands before execution
- Persisting selected common preferences between dialogs

### How to install

**Starting from Rider 2023.3, the plugin is bundled into the main Rider distribution. No additional actions required.**

### How to use

1. Open solution that contains EF Core related projects (migrations and startup projects)
2. Navigate to <kbd>Entity Framework Core</kbd> under project or solution context menu:

   ![image](https://github.com/JetBrains/rider-efcore/assets/20597871/6356b447-b84d-45fb-b6a3-7babd9f95280)

   You could use the context menu of either your migrations or startup projects.
3. Clicking action will show the appropriate dialog:

   ![image](https://github.com/JetBrains/rider-efcore/assets/20597871/17d98128-f347-48e8-b30b-fb3d6c2fbb3a)

4. After you press <kbd>Ok</kbd>, the selected action will be executed in a console-like window (by default):

   ![image](https://github.com/JetBrains/rider-efcore/assets/20597871/e520fa69-3565-4487-9872-e19df9979b48)

5. You could also configure the dialog's behavior in Settings, under <kbd>Tools</kbd>/<kbd>EF Core UI</kbd> section:

   ![image](https://github.com/JetBrains/rider-efcore/assets/20597871/31a23fd1-c0ad-404d-9a58-4b839bbabe87)

More about features and available dialogs you could read on [**the documentation page**](https://www.jetbrains.com/help/rider/Visual_interface_for_EF_Core_commands.html).

### Requirements

#### TL;DR:
- the most recent stable version of **JetBrains Rider**,
- [officially supported](https://dotnet.microsoft.com/en-us/platform/support/policy/dotnet-core#lifecycle) versions of **.NET**, **EF Core tools** and **EF Core NuGets**.

Before opening the issue, please make sure that your projects and development environment completely satisfies these requirements:

- Target frameworks
  - `net8.0` (preview)
  - `net7.0`
  - `net6.0`
  - `net5.0`
  - `netcoreapp3.1`
  - `netstandard2.1` <kbd>*</kbd>
  - `netstandard2.0` <kbd>*</kbd>

  <kbd>*</kbd>: only for Migrations projects

- `Microsoft.EntityFrameworkCore.*`: **5.0.0 or higher**

- Tools (`dotnet ef`): **5.0 or higher**

### Development

> **Note**: You should have JDK 17 and .NET SDK 7.0+ installed and configured.

#### Preparing

`./gradlew rdgen` - generates RD protocol data for plugin internal communication

#### Building plugin parts

`./gradlew buildPlugin`

It will build both frontend and backend parts.

#### Running

Next command will start instance of JetBrains Rider with plugin attached to it:

`./gradlew runIde`

### Contributing

Contributions are welcome! 🎉

It's better to create an issue with description of your bug/feature before creating pull requests.

### See also

- [**Entity Framework Core inside Rider: UI Way**](https://blog.jetbrains.com/dotnet/2022/01/31/entity-framework-core-inside-rider-ui-way/) _(recommended tutorial)_
- [**Marketplace page**](https://plugins.jetbrains.com/plugin/18147-entity-framework-core-ui)
- [**Changelog**](CHANGELOG.md)
