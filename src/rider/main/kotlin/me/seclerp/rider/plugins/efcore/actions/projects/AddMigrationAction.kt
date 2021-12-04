package me.seclerp.rider.plugins.efcore.actions.projects

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.MigrationsClient
import me.seclerp.rider.plugins.efcore.dialogs.AddMigrationDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.executeCommandUnderProgress

class AddMigrationAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            AddMigrationDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val migrationsClient = intellijProject.getService<MigrationsClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Creating migration...", "New migration has been created") {
                migrationsClient.add(commonOptions, dialog.migrationName)
            }
        }
    }
}