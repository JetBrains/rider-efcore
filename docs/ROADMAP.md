## Feature readmap
### 1.0.0
- Creating migrations
- Updating database with selected migration (including migration names autocompletion)
- Removing last created migration
- Persisting selected migrations and startup projects between dialogs
- Suggesting installing dotnet `ef command` line tools if not installed (when opening solution that contains EF Core related projects)
- Deleting used database

### 1.1.0
- DB-first scaffolding with all available customization options
- Add ability to specify folder for migrations on "Add migration"

## Suggestions

> Suggestions are not submitted completely and need to be discussed/revisited

- Add "Remove All Migrations" action
- Add "Squash Migrations", something similar to git squash for commits on merge
- Support .NET Core 3.1 and .NET Standard 2.1 projects
- Pop-up with quick access to actions related to last used project pair
- Remove Migrations folder after Remove Last Migration if there are no migrations left
