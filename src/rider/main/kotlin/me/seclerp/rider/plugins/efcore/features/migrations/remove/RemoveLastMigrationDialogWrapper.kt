package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper

class RemoveLastMigrationDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectName: String?,
) : CommonDialogWrapper<RemoveLastMigrationDataContext>(
    RemoveLastMigrationDataContext(intellijProject),
    toolsVersion,
    "Remove Last Migration",
    intellijProject,
    selectedProjectName,
    true
) {
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Constructor
    init {
        initUi()
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()

        return migrationsCommandFactory.removeLast(commonOptions)
    }

    override fun postCommandExecute(commandResult: CliCommandResult) {
        if (!commandResult.succeeded) {
            return
        }

        val folderService = intellijProject.service<RemoveLastMigrationFolderService>()
        folderService.deleteMigrationsFolderIfEmpty(dataCtx.availableMigrations.value.firstOrNull())
    }
}