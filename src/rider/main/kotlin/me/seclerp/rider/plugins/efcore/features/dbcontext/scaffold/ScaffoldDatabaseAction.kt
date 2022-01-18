package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.api.DbContextClient
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction

class ScaffoldDatabaseAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            ScaffoldDatabaseDialogWrapper(efCoreVersion, model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val dbContextClient = intellijProject.getService<me.seclerp.rider.plugins.efcore.cli.api.DbContextClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Updating database...", "Database has been updated") {
                dbContextClient.scaffold(
                    efCoreVersion, commonOptions,
                    dialog.connection,
                    dialog.provider,
                    dialog.outputFolder,
                    dialog.useAttributes,
                    dialog.useDatabaseNames,
                    dialog.generateOnConfiguring,
                    dialog.usePluralizer,
                    dialog.dbContextName,
                    dialog.dbContextFolder,
                    dialog.scaffoldAllTables,
                    dialog.tablesList.map { it.data },
                    dialog.scaffoldAllSchemas,
                    dialog.schemasList.map { it.data })
            }
        }
    }
}