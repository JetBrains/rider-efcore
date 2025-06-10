package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import com.jetbrains.observables.*
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService

class AddMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val migrationName = observable("")
    val migrationsOutputFolder = observable("Migrations")
    val openMigrationFile = observable(false)

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()

        migrationName.bind(availableMigrations) {
            if (it.isEmpty() && migrationName.value.trim() == "")
                "Initial"
            else
                migrationName.value
        }
    }

    override fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.loadState(commonDialogState)

        commonDialogState.get(KnownStateKeys.OUTPUT_FOLDER)?.apply {
            migrationsOutputFolder.value = this
        }
        commonDialogState.getBool(KnownStateKeys.OPEN_MIGRATION_FILE)?.apply {
            openMigrationFile.value = this
        }
    }

    override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.saveState(commonDialogState)

        commonDialogState.set(KnownStateKeys.OUTPUT_FOLDER, migrationsOutputFolder.value)
        commonDialogState.set(KnownStateKeys.OPEN_MIGRATION_FILE, openMigrationFile.value)
    }

    object KnownStateKeys {
        const val OUTPUT_FOLDER = "outputFolder"
        const val OPEN_MIGRATION_FILE = "openFile"
    }
}
