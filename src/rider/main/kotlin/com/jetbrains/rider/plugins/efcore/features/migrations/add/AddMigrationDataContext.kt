package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.jetbrains.observables.*
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService

class AddMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true, false) {
    private val commandFactory = intellijProject.service<MigrationsCommandFactory>()
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val migrationName = observable("")
    val migrationsOutputFolder = observable("Migrations")

    val migrationNameValidation : (String) -> ValidationInfo? = {
        if (it.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migration.name.could.not.be.empty"))
        else if (availableMigrations.value.any { migration -> migration.migrationLongName == it.trim() })
            error(EfCoreUiBundle.message("dialog.message.migration.with.such.name.already.exist"))
        else
            null
    }

    val migrationsOutputFolderValidation : (String) -> ValidationInfo? = {
        if (it.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migrations.output.folder.could.not.be.empty"))
        else
            null
    }

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

    override fun validate() = buildList {
        migrationNameValidation(migrationName.value)?.let { add(it) }
        migrationsOutputFolderValidation(migrationName.value)?.let { add(it) }
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val migrationName = migrationName.value.trim()
        val migrationsOutputFolder = migrationsOutputFolder.value

        return commandFactory.add(commonOptions, migrationName, migrationsOutputFolder)
    }

    object KnownStateKeys {
        const val OUTPUT_FOLDER = "outputFolder"
    }
}
