package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.features.shared.TryCommandAgainAction

public class NotificationCommandResultProcessor(
    private val project: Project,
    private val succeedText: String,
    private val shouldRefreshSolution: Boolean = true
) : CliCommandResultProcessor() {

    override fun doProcess(result: CliCommandResult, retryAction: () -> Unit) {
        if (result.succeeded) {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(succeedText, NotificationType.INFORMATION)
                .notify(project)
        } else {
            val errorTextBuilder = StringBuilder()
            errorTextBuilder.append("Command: ${result.command}")

            if (result.output.trim().isNotEmpty())
                errorTextBuilder.append("\n\nOutput:\n${result.output}")

            if (result.error?.trim()?.isNotEmpty() == true)
                errorTextBuilder.append("\n\nError:\n${result.error}")

            errorTextBuilder.append("\n\nExit code: ${result.exitCode}")

            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(
                    "EF Core command failed",
                    errorTextBuilder.toString(),
                    NotificationType.ERROR
                )
                .addAction(TryCommandAgainAction(retryAction))
                .notify(project)
        }

        if (shouldRefreshSolution) {
            ApplicationManager.getApplication().invokeAndWait {
                refreshSolution()
            }
        }
    }

    private fun refreshSolution() {
        FileDocumentManager.getInstance().saveAllDocuments()
        SaveAndSyncHandler.getInstance().refreshOpenFiles()
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
    }
}