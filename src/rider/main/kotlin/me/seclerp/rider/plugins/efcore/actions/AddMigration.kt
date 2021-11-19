package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.dialogs.AddMigrationDialogWrapper

class AddMigration : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = true
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val model = actionEvent.project?.solution?.riderEfCoreModel
        val projects = model?.getProjectNames?.sync(Unit)

    val dialog = AddMigrationDialogWrapper(projects?.toTypedArray() ?: emptyArray())
        if (dialog.showAndGet()) {
            // TODO
        }
    }
}