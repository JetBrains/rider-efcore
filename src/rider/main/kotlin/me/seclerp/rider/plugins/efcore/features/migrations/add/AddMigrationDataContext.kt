package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

class AddMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val migrationName = observable("")
    val migrationsOutputFolder = observable("Migrations")

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
    }

    override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.saveState(commonDialogState)

        commonDialogState.set(KnownStateKeys.OUTPUT_FOLDER, migrationsOutputFolder.value)
    }

    object KnownStateKeys {
        const val OUTPUT_FOLDER = "outputFolder"
    }
}
