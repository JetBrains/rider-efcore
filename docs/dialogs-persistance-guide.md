## How to implement dialogs data persistence

This is internal documentation showing how to use current state management to end up with additional dialog options persistence.

### Data context

Each dialog has a **data context** - an object that exists between UI layer with controls and Rider backend
(where all .NET-related analysis for migrations, DbContexts, etc. lives).

Data context looks like that:

```kotlin
class ScaffoldDbContextDataContext(intellijProject: Project) : CommonDataContext(intellijProject, false) {
    val connection = observable("")
    val provider = observable("")
    val outputFolder = observable("Entities")

    val useAttributes = observable(false)
    val useDatabaseNames = observable(false)
    val generateOnConfiguring = observable(true)
    val usePluralizer = observable(true)

    val dbContextName = observable("MyDbContext")
    val dbContextFolder = observable("Context")

    val tablesList = observableList<SimpleItem>()
    val schemasList = observableList<SimpleItem>()

    val scaffoldAllTables = observable(true)
    val scaffoldAllSchemas = observable(true)
}
```

Logic for saving/restoring dialogs state is related for those contexts.

### `loadState` and `saveState`

Two methods used to restore and save dialogs state.

You should use `DialogsStateService.SpecificDialogState` to retrieve and save data.

> **Note**: Not all properties presented in a dialog are suitable for saving.
Some of them are for single-time usage and should not be saved.
> > For example, `Migration's Name` in `Add Migration` dialog.

Typical implementation for particular field should looks like that:

```kotlin
val someProperty = observable("")

override fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
    super.loadState()
    
    val somePropertyName = commonDialogState.get(KnownStateKeys.SOME_PROPERTY)
    val somePropertyValue = availableProperties.firstOrNull { it == somePropertyName }
    if (somePropertyValue != null) {
        this.someProperty.value = somePropertyValue
    }
}

override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
    super.saveState()

    val somePropertyValue = someProperty.value
    if (somePropertyValue != null) {
        commonDialogState.set(KnownStateKeys.SOME_PROPERTY, somePropertyValue)
    }
}

private object KnownStateKeys {
    const val SOME_PROPERTY = "SOME_PROPERTY"
}
```

> **Note**: don't forget to call `super.loadState` and `super.saveState`, base implementation is responsible for
> saving/restoring state of common dialog properties. such as Migrations project, Startup project, Build configuration, etc.

### Examples

See `CommonDataContext.kt` for common options persistence implementation.