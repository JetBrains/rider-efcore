package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.DatabaseClient
import me.seclerp.rider.plugins.efcore.dialogs.UpdateDatabaseDialogWrapper

class UpdateDatabaseAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent, intellijProject) {
            val model = getEfCoreRiderModel(actionEvent)
            UpdateDatabaseDialogWrapper(intellijProject, model, currentProject, migrationsProjects, startupProjects)
        }

        if (dialog.showAndGet()) {
            val databaseClient = intellijProject.getService<DatabaseClient>()
            val commonOptions = getCommonOptions(dialog)
            val targetMigration = dialog.targetMigration
            val connection = if (dialog.useDefaultConnection) null else dialog.connection

            executeCommandUnderProgress(intellijProject, "Updating database...", "Database has been updated") {
                databaseClient.update(commonOptions, targetMigration, connection)
            }
        }
    }
}