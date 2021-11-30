package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.DatabaseClient
import me.seclerp.rider.plugins.efcore.dialogs.DropDatabaseDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.executeCommandUnderProgress

class DropDatabaseAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            DropDatabaseDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val databaseClient = intellijProject.getService<DatabaseClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Deleting database...", "Database has been deleted") {
                databaseClient.drop(commonOptions)
            }
        }
    }
}