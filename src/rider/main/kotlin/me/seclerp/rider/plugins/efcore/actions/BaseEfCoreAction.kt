package me.seclerp.rider.plugins.efcore.actions

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.rd.ide.model.ProjectInfo
import com.jetbrains.rd.ide.model.RiderEfCoreModel
import com.jetbrains.rd.ide.model.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.dialogs.BaseEfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.CommonOptions

abstract class BaseEfCoreAction: AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isProjectFile()
    }

    fun getCommonOptions(dialog: BaseEfCoreDialogWrapper): CommonOptions =
        CommonOptions(
            dialog.migrationsProject!!.fullPath,
            dialog.startupProject!!.fullPath,
            dialog.noBuild
        )

    protected fun executeCommandUnderProgress(project: Project, taskTitle: String, succeedText: String, what: (Unit) -> CliCommandResult) {
        runBackgroundableTask(taskTitle, project, false) {
            val result = what(Unit)
            if (result.succeeded) {
                NotificationGroupManager.getInstance().getNotificationGroup("EF Core Notifications Group")
                    .createNotification(succeedText, NotificationType.INFORMATION)
                    .notify(project)
            } else {
                NotificationGroupManager.getInstance().getNotificationGroup("EF Core Notifications Group")
                    .createNotification(
                        "EF Core command failed",
                        "Command: ${result.command}\n\nOutput:\n${result.output}\n\nExit code: ${result.exitCode}",
                        NotificationType.ERROR)
                    .notify(project)
            }

            ApplicationManager.getApplication().invokeLater {
                refreshSolution()
            }
        }
    }

    protected fun getEfCoreRiderModel(actionEvent: AnActionEvent): RiderEfCoreModel {
        // TODO: Validate

        return actionEvent.project?.solution?.riderEfCoreModel!!
    }

    protected fun <R> buildDialogInstance(actionEvent: AnActionEvent, dialogFactory: DialogBuildParameters.() -> R): R {
        val model = getEfCoreRiderModel(actionEvent)
        val currentDotnetProjectName = actionEvent.getDotnetProjectName()
        // TODO: Handle case when there is no appropriate projects
        val params = DialogBuildParameters(model, currentDotnetProjectName)

        return dialogFactory(params)
    }

    @Suppress("ArrayInDataClass")
    data class DialogBuildParameters(
        val model: RiderEfCoreModel,
        val currentDotnetProjectName: String)

    private fun refreshSolution() {
        FileDocumentManager.getInstance().saveAllDocuments()
        SaveAndSyncHandler.getInstance().refreshOpenFiles()
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
    }
}