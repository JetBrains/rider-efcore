package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction

class UpdateDatabaseAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            UpdateDatabaseDialogWrapper(efCoreVersion, model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val databaseClient = intellijProject.getService<me.seclerp.rider.plugins.efcore.cli.api.DatabaseClient>()
            val commonOptions = getCommonOptions(dialog)
            val targetMigration = dialog.model.targetMigration.trim()
            val connection = if (dialog.model.useDefaultConnection) null else dialog.model.connection

            executeCommandUnderProgress(intellijProject, "Updating database...", "Database has been updated") {
                databaseClient.update(efCoreVersion, commonOptions, targetMigration, connection)
            }
        }
    }
}