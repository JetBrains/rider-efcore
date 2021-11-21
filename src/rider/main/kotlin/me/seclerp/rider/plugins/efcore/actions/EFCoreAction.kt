package me.seclerp.rider.plugins.efcore.actions

import com.intellij.ide.actions.SynchronizeAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rd.ide.model.OperationResult

abstract class EFCoreAction: AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isProjectFile()
    }

    protected fun <R> execute(actionEvent: AnActionEvent, completedText: String, what: (Unit) -> R): R {
        val result = what(Unit)

        if (result is OperationResult) {
            if (result.succeeded) {
                NotificationGroupManager.getInstance().getNotificationGroup("EF Core Notifications Group")
                    .createNotification(completedText, NotificationType.INFORMATION)
                    .notify(actionEvent.project)
            } else {
                NotificationGroupManager.getInstance().getNotificationGroup("EF Core Notifications Group")
                    .createNotification(
                        "EF Core command failed",
                        "Command: ${result.cliCommand}\nOutput: ${result.output}\nExit code: ${result.exitCode}",
                        NotificationType.ERROR)
                    .notify(actionEvent.project)
            }
        }

        refreshSolution(actionEvent)

        return result
    }

    private fun refreshSolution(actionEvent: AnActionEvent) {
        SynchronizeAction().actionPerformed(actionEvent)
    }
}