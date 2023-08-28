package com.jetbrains.rider.plugins.efcore.features.database.update

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.jetbrains.observables.bind
import com.jetbrains.observables.observable
import com.jetbrains.observables.observableList
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableConnections
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import org.jetbrains.annotations.NonNls

class UpdateDatabaseDataContext(intellijProject: Project, private val efCoreVersion: DotnetEfVersion): CommonDataContext(intellijProject, true, true) {
    val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

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

    val targetMigrationValidation: (String?) -> ValidationInfo? = {
        if (it.isNullOrEmpty())
            ValidationInfo(EfCoreUiBundle.message("dialog.message.target.migration.should.be.specified"))
        else
            null
    }

    val connectionValidation: (String?) -> ValidationInfo? = {
        if (it.isNullOrEmpty())
            ValidationInfo(EfCoreUiBundle.message("dialog.message.connection.could.not.be.empty"))
        else null
    }

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

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val targetMigration = targetMigration.value!!.trim()
        val connection = if (useDefaultConnection.value) null else connection.value

        return databaseCommandFactory.update(efCoreVersion, commonOptions, targetMigration, connection)
    }

    override fun validate() = buildList {
        addAll(super.validate())
        targetMigrationValidation(targetMigration.value)?.let { add(it) }
        connectionValidation(connection.value)?.let { add(it) }
    }

    object KnownStateKeys {
        @NonNls
        const val USE_DEFAULT_CONNECTION = "useDefaultConnection"
        @NonNls
        const val CONNECTION = "connection"
    }
}