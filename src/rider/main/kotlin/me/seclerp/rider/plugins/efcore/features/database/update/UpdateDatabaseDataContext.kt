package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

class UpdateDatabaseDataContext(intellijProject: Project): CommonDataContext(intellijProject, true) {
    val observableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)

    val availableMigrationNames = observableList<String>()
        .apply {
            bind(observableMigrations) { migrations -> migrations
                .map { it.migrationLongName }
                .sortedByDescending { it }
                .apply { add("0") }
            }

        }

    var targetMigration = observable<String?>(null)
    var useDefaultConnection = observable(true)
    var connection = observable("")

    override fun initBindings() {
        super.initBindings()

        observableMigrations.initBinding()
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
        const val USE_DEFAULT_CONNECTION = "useDefaultConnection"
        const val CONNECTION = "connection"
    }
}