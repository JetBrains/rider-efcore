package me.seclerp.rider.plugins.efcore.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.showYesNoDialog
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress


class RemoveLastMigrationAction: EFCoreAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val projectName = actionEvent.getDotnetProjectName()
        val intellijProject = actionEvent.project!!
        if (showYesNoDialog(
                title = "Remove Last Migration",
                message = "Are you sure that you want to delete last created migration for project \"${projectName}\"?",
                project = actionEvent.project)) {

            val model = actionEvent.project?.solution?.riderEfCoreModel

            model!!.removeLastMigration.runUnderProgress(projectName, intellijProject, "Removing migration...",
                isCancelable = false,
                throwFault = true
            );

            NotificationGroupManager.getInstance().getNotificationGroup("EF Core Notifications Group")
                .createNotification("Last migration has been removed", NotificationType.INFORMATION)
                .notify(actionEvent.project);

            val actionFile = actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)!!
            actionFile.parent.refresh(true, true)
        }
    }
}