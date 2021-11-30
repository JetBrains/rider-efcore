package me.seclerp.rider.plugins.efcore.commands

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups

fun executeCommandUnderProgress(
    project: Project, taskTitle: String, succeedText: String, shouldRefreshSolution: Boolean = true,
    what: (Unit) -> CliCommandResult
) {
    runBackgroundableTask(taskTitle, project, false) {
        val result = what(Unit)
        if (result.succeeded) {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(succeedText, NotificationType.INFORMATION)
                .notify(project)
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(
                    "EF Core command failed",
                    "Command: ${result.command}\n\nOutput:\n${result.output}\n\nError:${result.error}\n\nExit code: ${result.exitCode}",
                    NotificationType.ERROR)
                .notify(project)
        }

        if (shouldRefreshSolution) {
            ApplicationManager.getApplication().invokeAndWait {
                refreshSolution()
            }
        }
    }
}

private fun refreshSolution() {
    FileDocumentManager.getInstance().saveAllDocuments()
    SaveAndSyncHandler.getInstance().refreshOpenFiles()
    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
}