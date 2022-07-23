package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.api.DbContextClient
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction

class ScaffoldDbContextAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            ScaffoldDbContextDialogWrapper(efCoreVersion, model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val dbContextClient = intellijProject.getService<DbContextClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Scaffolding DbContext...", "DbContext has been scaffolded") {
                val model = dialog.model
                dbContextClient.scaffold(
                    efCoreVersion, commonOptions,
                    model.connection,
                    model.provider,
                    model.outputFolder,
                    model.useAttributes,
                    model.useDatabaseNames,
                    model.generateOnConfiguring,
                    model.usePluralizer,
                    model.dbContextName,
                    model.dbContextFolder,
                    model.scaffoldAllTables,
                    model.tablesList.map { it.data },
                    model.scaffoldAllSchemas,
                    model.schemasList.map { it.data },
                    model.overrideExisting)
            }
        }
    }
}