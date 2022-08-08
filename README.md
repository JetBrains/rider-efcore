<div align="center">
  <h1>Entity Framework Core UI plugin for JetBrains Rider</h1>
  <img src="img/rider-efcore-cover.png" alt="Logo">
  <br /><br />
  This plugin introduces Entity Framework Core commands' UI inside JetBrains Rider.
  <br /><br />
  <a href="https://github.com/seclerp/rider-efcore/actions/workflows/build.yml"><img src="https://github.com/seclerp/rider-efcore/actions/workflows/build.yml/badge.svg" alt="Build"></a>
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

#### Using marketplace:

> **For EAP users**: you should add `https://plugins.jetbrains.com/plugins/eap/list` to your plugin repositories list before installing

1. Go to `Settings` / `Plugins` / `Marketplace`
1. Search for "Entity Framework Core UI"
1. Click `Install`, then `Save`
1. After saving restart Rider

#### Using `.zip` file
1. Go to [**Releases**](https://github.com/seclerp/rider-efcore/releases)
2. Download the latest release of plugin for your edition of JetBrains Rider (Stable or EAP)
3. Proceed to `Settings` / `Plugins` / `⚙` / `Install plugin from disk`
4. Click `Save`
5. After saving restart Rider

### How to use

1. Open solution that contains EF Core related projects (migrations and startup projects)
2. Navigate to <kbd>Tools</kbd>/<kbd>Entity Framework Core</kbd> under project context menu:

   ![Logo](img/how-to-use-1.png)

   You could use context menu of either your migrations or startup project.
3. Clicking action will show appropriate dialog:

   ![Logo](img/how-to-use-2.png)
4. After you press <kbd>Ok</kbd>, selected action will be executed in background:

   ![Logo](img/how-to-use-3.png)

   ![Logo](img/how-to-use-4.png)

More about features and available dialogs you could read in [**the blog post**](https://blog.seclerp.me/general/working-with-ef-core-inside-jetbrains-rider/).

### Requirements

- JetBrains Rider **2022.1+**

  or JetBrains Rider **2022.2 EAP**
- EF Core **5.0+** with projects under such target frameworks:
  - `net7.0` (preview)
  - `net6.0`
  - `net5.0`
  - `netcoreapp3.1`
  - `netstandard2.0` (only for Migrations projects)
  - `netstandard2.1` (only for Migrations projects)


- EF Core global tools (`dotnet ef`) **5.0+** installed

> **Note**: Projects with older versions of EF Core might work, but with issues

### Development

> **Note**: You should have JDK 11 and .NET SDK 5.0+ installed and configured.

#### Preparing

`./gradlew rdgen` - generates RD protocol data for plugin internal communication

#### Building plugin parts

- for stable version of Rider:

  `./gradlew buildPlugin`


- for EAP version of Rider:

  `./gradlew buildPlugin -PRiderSdkVersion=2022.2.0-eap04 -PProductVersion=2022.2-EAP4-SNAPSHOT`

It will build both frontend and backend parts.

#### Running

Next command will start instance of JetBrains Rider with plugin attached to it:

`./gradlew runIde`

### Contributing

Contributions are welcome! 🎉

It's better to create an issue with description of your bug/feature before creating pull requests.

#### About branching

This project uses customized git strategy.

Each `release/*` branch plays main development branch role for specific release.

For example, `release/222` means that branch is related to `222.*` release cycle for `2022.2` Rider version.

### See also

- [**Entity Framework Core inside Rider: UI Way**](https://blog.jetbrains.com/dotnet/2022/01/31/entity-framework-core-inside-rider-ui-way/) _(recommended tutorial)_
- [**Marketplace page**](https://plugins.jetbrains.com/plugin/18147-entity-framework-core-ui)
- [**Changelog**](CHANGELOG.md)
