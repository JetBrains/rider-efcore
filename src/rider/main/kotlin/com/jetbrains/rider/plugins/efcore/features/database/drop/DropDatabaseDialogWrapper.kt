package com.jetbrains.rider.plugins.efcore.features.database.drop

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import java.util.*

class DropDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<CommonDataContext>(
    CommonDataContext(intellijProject, true),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.Database.DropDatabaseAction.text"),
    intellijProject,
    selectedProjectId,
    false
) {
    private val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

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

    override fun generateCommand(): GeneralCommandLine {
        val options = getCommonOptions()
        return databaseCommandFactory.drop(options)
    }
}