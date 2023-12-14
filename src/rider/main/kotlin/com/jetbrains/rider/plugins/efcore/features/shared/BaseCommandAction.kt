package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.launchOnUi
import com.intellij.openapi.rd.util.lifetime
import com.intellij.openapi.rd.util.withBackgroundContext
import com.jetbrains.rider.model.dotNetActiveRuntimeModel
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.cli.api.EfCoreCliCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider
import com.jetbrains.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.BaseDialogWrapper
import com.jetbrains.rider.plugins.efcore.features.shared.statistics.CommandUsageCollector
import com.jetbrains.rider.plugins.efcore.rd.ToolKind
import java.util.UUID

abstract class BaseCommandAction : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isEfCoreActionContext()
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project ?: return
        intellijProject.lifetime.launchOnUi {
            val version = fetchEfCoreVersion(intellijProject) ?: return@launchOnUi
            openDialog(actionEvent, version)
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    abstract fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?): BaseDialogWrapper

    private fun fetchEfCoreVersion(intellijProject: Project): DotnetEfVersion? {
        val efCoreDefinition = intellijProject.solution.riderEfCoreModel.cliToolsDefinition.valueOrNull
        val dotnetCliPath = intellijProject.solution.dotNetActiveRuntimeModel.activeRuntime.valueOrNull?.dotNetCliExePath
        val dotnetEfVersion = efCoreDefinition?.let { DotnetEfVersion.parse(it.version) }

        return when {
            dotnetCliPath == null -> {
                notifyDotnetIsNotInstalled(intellijProject)
                null
            }
            efCoreDefinition == null || efCoreDefinition.toolKind == ToolKind.None -> {
                notifyEfIsNotInstalled(intellijProject)
                null
            }
            dotnetEfVersion == null ->  {
                notifyUnknownEfIsInstalled(intellijProject, efCoreDefinition.version)
                null
            }
            else -> dotnetEfVersion
        }
    }

    private suspend fun openDialog(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project ?: return
        val model = actionEvent.project?.solution?.riderEfCoreModel ?: return
        val currentDotnetProjectName = actionEvent.actionDotnetProjectId
        val dialog = createDialog(intellijProject, efCoreVersion, model, currentDotnetProjectName)

        if (dialog.showAndGet()) {
            val executor = PreferredCommandExecutorProvider.getInstance(intellijProject).getExecutor()
            val command = dialog.generateCommand()
            val cliCommand = EfCoreCliCommandFactory.getInstance(intellijProject).create(command, efCoreVersion)
            CommandUsageCollector.withCommandActivity(intellijProject, command) {
                withBackgroundContext {
                    executor.execute(cliCommand)?.apply {
                        dialog.postCommandExecute(this)
                    }
                }
            }
        }
    }

    private fun notifyEfIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification(
                EfCoreUiBundle.message("notification.title.ef.core.tools.required"),
                EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"),
                NotificationType.ERROR)
            .addAction(InstallDotnetEfAction())
            .notify(intellijProject)
    }

    private fun notifyUnknownEfIsInstalled(intellijProject: Project, version: String) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification(EfCoreUiBundle.message("notification.content.invalid.ef.core.tools.version", version), NotificationType.ERROR)
            .addAction(InstallDotnetEfAction())
            .notify(intellijProject)
    }

    private fun notifyDotnetIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification(EfCoreUiBundle.message("notification.content.net.net.core.required.to.execute.this.action"), NotificationType.ERROR)
            .notify(intellijProject)
    }
}