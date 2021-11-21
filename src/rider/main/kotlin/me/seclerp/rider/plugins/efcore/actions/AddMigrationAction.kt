package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.AddMigrationOptions
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.dialogs.AddMigrationDialogWrapper

class AddMigrationAction : EFCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        val model = actionEvent.project?.solution?.riderEfCoreModel!!
        val projects = model.getProjectNames.sync(Unit)
        val projectName = actionEvent.getDotnetProjectName()
        val projectsArray = projects.toTypedArray()
        val dialog = AddMigrationDialogWrapper(intellijProject.lifetime, projectName, projectsArray)
        if (dialog.showAndGet()) {
            val options = AddMigrationOptions(dialog.migrationName, dialog.migrationsProject, dialog.startupProject, dialog.noBuild)
            execute(actionEvent, "New migration created") {
                model.addMigration.runUnderProgress(options, intellijProject, "Creating migration...",
                    isCancelable = false,
                    throwFault = true
                )
            }
        }
    }
}