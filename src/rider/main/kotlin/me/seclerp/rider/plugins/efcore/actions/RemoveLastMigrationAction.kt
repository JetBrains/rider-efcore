package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.MigrationsClient
import me.seclerp.rider.plugins.efcore.dialogs.RemoveLastMigrationDialogWrapper

class RemoveLastMigrationAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = getDialogInstance(actionEvent)

        if (dialog.showAndGet()) {
            val migrationsClient = intellijProject.getService<MigrationsClient>()
            val commonOptions = getCommonOptions(dialog)

            executeCommandUnderProgress(intellijProject, "Removing migration...", "Last migration has been removed") {
                migrationsClient.removeLast(commonOptions)
            }
        }
    }

    private fun getDialogInstance(actionEvent: AnActionEvent): RemoveLastMigrationDialogWrapper {
        val model = getEfCoreRiderModel(actionEvent)
        val migrationsProject = model.getAvailableMigrationsProjects.sync(Unit).toTypedArray()
        val startupProject = model.getAvailableStartupProjects.sync(Unit).toTypedArray()
        // TODO: Handle case when there is no appropriate projects
        val dotnetProject = migrationsProject.find { it.name == actionEvent.getDotnetProjectName() } ?: migrationsProject.first()

        return RemoveLastMigrationDialogWrapper(dotnetProject, migrationsProject, startupProject)
    }
}