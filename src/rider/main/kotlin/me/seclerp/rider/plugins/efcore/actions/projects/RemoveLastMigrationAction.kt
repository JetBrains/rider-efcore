package me.seclerp.rider.plugins.efcore.actions.projects

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.MigrationsClient
import me.seclerp.rider.plugins.efcore.dialogs.RemoveLastMigrationDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.executeCommandUnderProgress

class RemoveLastMigrationAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            RemoveLastMigrationDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val migrationsClient = intellijProject.getService<MigrationsClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Removing migration...", "Last migration has been removed") {
                migrationsClient.removeLast(commonOptions)
            }
        }
    }
}