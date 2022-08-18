package me.seclerp.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import me.seclerp.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper

class DropDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?,
) : BaseDialogWrapper(toolsVersion, "Drop Database", intellijProject, selectedDotnetProjectName, false) {

    val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

    //
    // Constructor
    init {
        init()
    }

    override fun doOKAction() {
        if (showYesNoDialog(
            "Confirmation",
            "Are you sure that you want to drop database, used by ${commonCtx.dbContext.value!!.name}? This action can't be undone.",
            intellijProject)
        ) {
            super.doOKAction()
        }
    }

    override fun generateCommand(): CliCommand {
        val options = getCommonOptions()
        return databaseCommandFactory.drop(options)
    }
}