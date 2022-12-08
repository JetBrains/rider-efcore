## Which values are saved between dialogs?

> **Note**: To save selected values between dialogs the
> `Settings` | `Tools` | `EF Core UI` | `Use previously selected options in dialogs` option should be **enabled**.


### Common
- **Startup project**: Yes _(determined by previously selected pair with Migrations project)_
- **Migrations project**: Yes _(determined by previously selected pair with Startup project)_
- **DbContext value**: Yes _(per Migrations project)_
- **Build configuration**: No _(reason: default value equals to current solution configuration)_
- **SKip project build process**: Yes
- **Target framework**: Yes _(per Startup project)_
- **Additional arguments**: Yes _(when "Store sensitive data in a secure store" option is enabled)_


### Add Migration
- **Migration name**: No
- **Migrations folder**: Yes


### Remove Last Migration
Nothing to save.


### Generate SQL Script
- **From migration**: No
- **To migration**: No
- **Output file**: Yes
- **Make script idempotent**: Yes
- **No transactions**: Yes


### Update Database
- **Target migration**: No
- **Use default connection of startup project**: Yes
- **Connection**: Yes _(when "Store sensitive data in a secure store" option is enabled)_


### Drop Database
Nothing to save.


### Scaffold DbContext

#### Main
- **Connection**: Yes _(when "Store sensitive data in a secure store" option is enabled)_
- **Provider**: Yes
- **Output folder**: Yes
- **Use attributes to generate the model**: Yes
- **Use database names**: Yes
- **Generate OnConfiguring method**: Yes
- **Use the pluralizer**: Yes

#### DbContext
- **Generated DbContext name**: Yes
- **Generated DbContext folder**: Yes

#### Tables
- **Scaffold all schemas**: No

#### Schemas
- **Scaffold all schemas**: No