package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import java.util.*

class RemoveLastMigrationDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<RemoveLastMigrationDataContext>(
    RemoveLastMigrationDataContext(intellijProject),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.Migrations.RemoveLastMigrationAction.text"),
    intellijProject,
    selectedProjectId,
    true
) {
    init {
        initUi()
    }

    override fun getHelpId() = "EFCore.Features.Migrations.RemoveLastMigration"

    override fun generateCommand(): DialogCommand {
        val commonOptions = getCommonOptions()

        return RemoveLastMigrationCommand(commonOptions)
    }

    override fun postCommandExecute(commandResult: CliCommandResult) {
        if (!commandResult.succeeded)
            return

        val folderService = intellijProject.service<RemoveLastMigrationFolderService>()
        folderService.deleteMigrationsFolderIfEmpty(dataCtx.availableMigrations.value.firstOrNull())
    }
}