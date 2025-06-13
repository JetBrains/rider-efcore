# Motivation

There is a huge number of different design and implementation problems in the EF Core UI plugin:

1. Actions responsible not only for showing dialog but for handling the dialogs result
2. Dialogs responsible not only for UI and validation but for generating commands
3. Additional layer of complexity with mapping between data context observables and dialog observables
4. A lot of dirty hacks to amend the execution process (like postCommandExecute)
5. Unclear threading usage
6. Visible number of deprecated APIs usage, some of them are scheduled for removal 
7. Custom observables library, a lot of duplication with the one from the Rider/IntelliJ platform

# New Architecture

Right now plugin frontend architecture is partially based on MVVM pattern, mostly for dialog UI bindings.
The new architecture would make this concept mostly absolute - MVVM proved it's efficiency with UI projects with
different level of complexity.

## Actions

Command actions would be just a facade for showing the dialog. Their enabled/visible state would be still based on
surrounding project context (EF Core presence, EF Core toolset presence, projects applicability). They will no longer
be responsible for commands execution.

Sample (subject of change):
```kotlin
internal class AddMigrationAction : CommandAction(CommandDialogType.ADD_MIGRATION)
```

## Dialogs

Dialogs will become the central point of Views implementations from MVVM world. They will be responsible for:
1. Creating the data contexts (ViewModels) related to the specific dialog and their controls
2. Binding ViewModels to specific Views (UI components)
3. Exposing the data contexts to nested dialogs (like Preview dialog) and for the testing purposes

```kotlin
internal class AddMigrationDialog(
    private val dataContext: AddMigrationContext, 
    private val project: Project,
    private val lifetime: Lifetime
) : CommandDialog(dataContext, project, lifetime) {
    override suspend fun buildUi(): DialogPanel {
        // 1. Create bindings on data context
        // 2. Return newly created panel
    }
    
    // In case some additional logic is required for OK and Cancel, like dangerous action confirmation
    override suspend fun handleOk(): Boolean {}
    override suspend fun handleCancel(): Boolean {}
}
```

## Commands

Most of the commands in plugin are mapped to the corresponding EF Core design commands - Add migration, Update database,
etc. They will implement the Command pattern and will be executed in decoupled way from the dialog, indirectly.

```kotlin
internal class AddMigrationCommand : DotnetEfCommand {
    override suspend fun execute(lifetime: Lifetime) {
        // ...
    }
}
```

## Command handlers

They way how different kinds of commands should be executed is determined by the applicable handler. 

```kotlin
internal class DotnetEfCommandHandler : CommandHandler {
    override fun isApplicable(command: Command) = command is DotnetEfCommand
    
    override suspend fun handle(command: Command, lifetime: Lifetime) {
        val efCommand = command as? DotnetEfCommand ?: return
        // Make some preparations, do threading, do toolwindow manipulation, etc.
    }
}
```

Default handler is a dead-end one and it just launches command's `execute` on a background thread.

```kotlin
internal class DefaultCommandHandler : CommandHandler {
    override fun isApplicable(command: Command) = true
    
    override suspend fun handle(command: Command, lifetime: Lifetime) {
        withContext(Dispatchers.Background) {
            command.execute(lifetime)
        }
    }
}
```


## Providers (?)

All functionality related to gathering information, like EF Core toolset should be implemented as a service.