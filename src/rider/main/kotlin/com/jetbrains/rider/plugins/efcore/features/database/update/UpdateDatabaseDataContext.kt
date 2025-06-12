package com.jetbrains.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.observables.bind
import com.jetbrains.rider.plugins.efcore.observables.observable
import com.jetbrains.rider.plugins.efcore.observables.observableList
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableConnections
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import org.jetbrains.annotations.NonNls

class UpdateDatabaseDataContext(intellijProject: Project): CommonDataContext(intellijProject, true) {
    val observableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val availableMigrationNames = observableList<String>()
    val observableConnections = ObservableConnections(intellijProject, startupProject)

    val migrationNames = observableList<String>()
        .apply {
            bind(observableMigrations) { migrations -> migrations
                .map { it.migrationLongName }
                .sortedByDescending { it }
            }
        }


    var targetMigration = observable<String?>(null)
    var useDefaultConnection = observable(true)
    var connection = observable("")

    override fun initBindings() {
        super.initBindings()

        observableMigrations.initBinding()
        observableConnections.initBinding()

        availableMigrationNames.bind(migrationNames) {
            buildList {
                addAll(it)
                add("0")
            }
        }
    }

    override fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.loadState(commonDialogState)

        commonDialogState.getBool(KnownStateKeys.USE_DEFAULT_CONNECTION)?.apply {
            useDefaultConnection.value = this
        }

        if (pluginSettings.storeSensitiveData && !useDefaultConnection.value) {
            commonDialogState.getSensitive(KnownStateKeys.CONNECTION)?.apply {
                connection.value = this
            }
        }
    }

    override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.saveState(commonDialogState)

        commonDialogState.set(KnownStateKeys.USE_DEFAULT_CONNECTION, useDefaultConnection.value)

        if (pluginSettings.storeSensitiveData && !useDefaultConnection.value) {
            commonDialogState.setSensitive(KnownStateKeys.CONNECTION, connection.value)
        }
    }

    object KnownStateKeys {
        @NonNls
        const val USE_DEFAULT_CONNECTION = "useDefaultConnection"
        @NonNls
        const val CONNECTION = "connection"
    }
}