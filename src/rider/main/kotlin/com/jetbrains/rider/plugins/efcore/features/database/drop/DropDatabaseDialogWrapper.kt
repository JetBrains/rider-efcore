package com.jetbrains.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import java.util.*

class DropDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<DropDatabaseDataContext>(
    DropDatabaseDataContext(intellijProject, true),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.Database.DropDatabaseAction.text"),
    intellijProject,
    selectedProjectId,
    false
) {
    private val databaseCommandFactory by lazy { DatabaseCommandFactory.getInstance(intellijProject) }

    //
    // Constructor
    init {
        initUi()
    }

    override fun doOKAction() {
        val projectName = dataCtx.migrationsProject.value?.name
        val dbContext = dataCtx.dbContext.value?.name
        val confirmationMessage =
            if (projectName != null && dbContext != null)
                EfCoreUiBundle.message("drop.database.confirmation.named", projectName, dbContext)
            else
                EfCoreUiBundle.message("drop.database.confirmation.selected")

        if (showYesNoDialog(EfCoreUiBundle.message("dialog.title.confirmation"), confirmationMessage, intellijProject)) {
            super.doOKAction()
        }
    }

    override fun generateCommand(): DialogCommand {
        val options = getCommonOptions()
        return DropDatabaseCommand(options)
    }
}