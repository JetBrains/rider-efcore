package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.clients.DatabaseClient
import me.seclerp.rider.plugins.efcore.dialogs.UpdateDatabaseDialogWrapper

class UpdateDatabaseAction : BaseEfCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val dialog = getDialogInstance(actionEvent, intellijProject)

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

    private fun getDialogInstance(actionEvent: AnActionEvent, intellijProject: Project): UpdateDatabaseDialogWrapper {
        val model = getEfCoreRiderModel(actionEvent)
        val migrationsProject = model.getAvailableMigrationsProjects.sync(Unit).toTypedArray()
        val startupProject = model.getAvailableStartupProjects.sync(Unit).toTypedArray()
        // TODO: Handle case when there is no appropriate projects
        val dotnetProject = migrationsProject.find { it.name == actionEvent.getDotnetProjectName() } ?: migrationsProject.first()

        return UpdateDatabaseDialogWrapper(intellijProject, model, dotnetProject, migrationsProject, startupProject)
    }
}