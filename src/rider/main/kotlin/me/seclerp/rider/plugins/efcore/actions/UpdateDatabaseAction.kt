package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.UpdateDatabaseOptions
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.dialogs.UpdateDatabaseDialogWrapper

class UpdateDatabaseAction: EFCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val model = actionEvent.project?.solution?.riderEfCoreModel!!
        val migrationsProject = model.getAvailableMigrationsProjects.sync(Unit).map { it.name }.toTypedArray()
        val startupProject = model.getAvailableStartupProjects.sync(Unit).map { it.name }.toTypedArray()
        val projectName = actionEvent.getDotnetProjectName()
        val dialog = UpdateDatabaseDialogWrapper(intellijProject, model, projectName, migrationsProject, startupProject)

        if (dialog.showAndGet()) {
            val options = UpdateDatabaseOptions(
                dialog.targetMigration,
                dialog.migrationsProjectName ?: "",
                dialog.startupProjectName ?: "",
                dialog.noBuild)

            execute(actionEvent, "Database has been updated") {
                model.updateDatabase.runUnderProgress(options, intellijProject, "Updating database...",
                    isCancelable = false,
                    throwFault = true
                )
            }
        }
    }
}