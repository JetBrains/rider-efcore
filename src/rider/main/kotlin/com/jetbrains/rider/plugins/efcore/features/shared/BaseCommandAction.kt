package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.dotNetActiveRuntimeModel
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.NotificationCommandResultProcessor
import com.jetbrains.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider
import com.jetbrains.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.BaseDialogWrapper
import com.jetbrains.rider.plugins.efcore.rd.ToolKind
import java.util.UUID

abstract class BaseCommandAction(
    private val actionPerformedText: String
) : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isEfCoreActionContext()
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project!!
        ProgressManager.getInstance().run(object : Task.Backgroundable(actionEvent.project, EfCoreUiBundle.message("progress.title.getting.dotnet.ef.version"), false) {
            override fun run(progress: ProgressIndicator) {
                val efCoreDefinition = intellijProject.solution.riderEfCoreModel.efToolsDefinition.valueOrNull
                val dotnetCliPath = intellijProject.solution.dotNetActiveRuntimeModel.activeRuntime.valueOrNull?.dotNetCliExePath

                if (dotnetCliPath == null) {
                    notifyDotnetIsNotInstalled(intellijProject)
                } else if (efCoreDefinition == null || efCoreDefinition.toolKind == ToolKind.None) {
                    notifyEfIsNotInstalled(intellijProject)
                } else {
                    val toolsVersion = DotnetEfVersion.parse(efCoreDefinition.version)!!
                    ApplicationManager.getApplication().invokeLater {
                        openDialog(actionEvent, toolsVersion)
                    }
                }
            }
        })
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    abstract fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?): BaseDialogWrapper

    private fun openDialog(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val model = getEfCoreRiderModel(actionEvent)
        val currentDotnetProjectName = actionEvent.getDotnetProjectId()
        val dialog = createDialog(intellijProject, efCoreVersion, model, currentDotnetProjectName)

        if (dialog.showAndGet()) {
            val executor = intellijProject.service<PreferredCommandExecutorProvider>().getExecutor()
            val command = dialog.generateCommand()
            val processor = NotificationCommandResultProcessor(
                intellijProject,
                actionPerformedText,
                true
            ).withPostExecuted {
                dialog.postCommandExecute(it)
            }

            executor.execute(command, processor)
        }
    }

    private fun getEfCoreRiderModel(actionEvent: AnActionEvent): RiderEfCoreModel {
        // TODO: Validate

        return actionEvent.project?.solution?.riderEfCoreModel!!
    }

    private fun notifyEfIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification(EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"), NotificationType.ERROR)
            .addAction(InstallDotnetEfAction())
            .notify(intellijProject)
    }

    private fun notifyDotnetIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification(EfCoreUiBundle.message("notification.content.net.net.core.required.to.execute.this.action"), NotificationType.ERROR)
            .notify(intellijProject)
    }
}