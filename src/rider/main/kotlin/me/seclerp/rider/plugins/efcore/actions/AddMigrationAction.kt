package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.dialogs.AddMigrationDialogWrapper

class AddMigrationAction : EFCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val model = actionEvent.project?.solution?.riderEfCoreModel
        val projects = model?.getProjectNames?.sync(Unit)
        val projectsArray = projects?.toTypedArray() ?: emptyArray()
        val dialog = AddMigrationDialogWrapper(projectsArray)
        if (dialog.showAndGet()) {
            // TODO Execute action
        }
    }
}