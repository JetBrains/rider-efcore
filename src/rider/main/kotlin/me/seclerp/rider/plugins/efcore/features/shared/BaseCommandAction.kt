package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.rd.ToolKind

abstract class BaseCommandAction(
    private val actionPerformingText: String,
    private val actionPerformedText: String
) : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isEfCoreActionContext()
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        ProgressManager.getInstance().run(object : Task.Backgroundable(actionEvent.project, "Getting dotnet ef version...", false) {
            override fun run(progress: ProgressIndicator) {
                val efCoreDefinition = intellijProject.solution.riderEfCoreModel.efToolsDefinition.valueOrNull

                if (efCoreDefinition == null || efCoreDefinition.toolKind == ToolKind.None) {
                    notifyDotnetEfIsNotInstalled(intellijProject)
                } else {
                    val toolsVersion = DotnetEfVersion.parse(efCoreDefinition.version)!!
                    ApplicationManager.getApplication().invokeLater {
                        openDialog(actionEvent, toolsVersion)
                    }
                }
            }
        })
    }

    abstract fun createDialog(
        intellijProject: Project,
        efCoreVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectName: String?): BaseDialogWrapper

    private fun openDialog(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val model = getEfCoreRiderModel(actionEvent)
        val currentDotnetProjectName = actionEvent.getDotnetProjectName()
        val dialog = createDialog(intellijProject, efCoreVersion, model, currentDotnetProjectName)

        if (dialog.showAndGet()) {
            executeCommandUnderProgress(actionEvent.project!!, actionPerformingText, actionPerformedText) {
                val result = dialog.generateCommand().execute()
                dialog.postCommandExecute(result)
                result
            }
        }
    }

    private fun getEfCoreRiderModel(actionEvent: AnActionEvent): RiderEfCoreModel {
        // TODO: Validate

        return actionEvent.project?.solution?.riderEfCoreModel!!
    }

    private fun notifyDotnetEfIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification("EF Core are required to execute this action", NotificationType.ERROR)
            .addAction(InstallDotnetEfAction())
            .notify(intellijProject)
    }
}