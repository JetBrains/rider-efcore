package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.AddMigrationOptions
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.dialogs.AddMigrationDialogWrapper

class AddMigrationAction : EFCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val model = actionEvent.project?.solution?.riderEfCoreModel!!
        val migrationsProject = model.getAvailableMigrationsProjects.sync(Unit).map { it.name }.toTypedArray()
        val startupProject = model.getAvailableStartupProjects.sync(Unit).map { it.name }.toTypedArray()
        val projectName = actionEvent.getDotnetProjectName()
        val dialog = AddMigrationDialogWrapper(projectName, migrationsProject, startupProject)
        if (dialog.showAndGet()) {
            val options = AddMigrationOptions(dialog.migrationName, dialog.migrationsProjectName, dialog.startupProjectName, dialog.noBuild)
            execute(actionEvent, "New migration created") {
                model.addMigration.runUnderProgress(options, intellijProject, "Creating migration...",
                    isCancelable = false,
                    throwFault = true
                )
            }
        }
    }
}