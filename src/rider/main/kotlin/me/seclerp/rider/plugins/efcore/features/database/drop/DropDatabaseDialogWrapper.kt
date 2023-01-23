package me.seclerp.rider.plugins.efcore.features.database.drop

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import me.seclerp.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import java.util.*

class DropDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<CommonDataContext>(
    CommonDataContext(intellijProject, false),
    toolsVersion,
    "Drop Database",
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
        if (showYesNoDialog(
            "Confirmation",
            "Are you sure that you want to drop database, used by ${dataCtx.migrationsProject.value?.name ?: "selected project"}? This action can't be undone.",
            intellijProject)
        ) {
            super.doOKAction()
        }
    }

    override fun generateCommand(): GeneralCommandLine {
        val options = getCommonOptions()
        return databaseCommandFactory.drop(options)
    }
}