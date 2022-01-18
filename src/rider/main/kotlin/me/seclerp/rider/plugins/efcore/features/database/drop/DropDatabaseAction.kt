package me.seclerp.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.api.DatabaseClient
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction

class DropDatabaseAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            DropDatabaseDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val databaseClient = intellijProject.getService<me.seclerp.rider.plugins.efcore.cli.api.DatabaseClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Deleting database...", "Database has been deleted") {
                databaseClient.drop(commonOptions)
            }
        }
    }
}