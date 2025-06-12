package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class RemoveLastMigrationAction : BaseCommandAction() {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = RemoveLastMigrationDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)

    override suspend fun executeCommand(cliCommand: CliCommand, intellijProject: Project): CliCommandResult? {
        val commandResult = super.executeCommand(cliCommand, intellijProject)
        if (commandResult == null || !commandResult.succeeded)
            return commandResult

        val folderService = intellijProject.service<RemoveLastMigrationFolderService>()
        folderService.deleteMigrationsFolderIfEmpty(dataCtx.availableMigrations.value.firstOrNull())
    }
}