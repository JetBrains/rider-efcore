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
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.rd.ToolKind

abstract class EfCoreAction : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isEfCoreActionContext()
    }

    fun getCommonOptions(dialog: EfCoreDialogWrapper): CommonOptions {
        val common = dialog.commonOptions
        return CommonOptions(
            common.migrationsProject!!.data.fullPath,
            common.startupProject!!.data.fullPath,
            common.dbContext?.data,
            common.buildConfiguration!!.displayName,
            common.targetFramework!!.data,
            common.noBuild
        )
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(actionEvent.project, "Getting dotnet ef version...", false) {
            override fun run(progress: ProgressIndicator) {
                val efCoreDefinition = actionEvent.project!!.solution.riderEfCoreModel.efToolsDefinition.valueOrNull

                if (efCoreDefinition == null || efCoreDefinition.toolKind == ToolKind.None) {
                    notifyDotnetEfIsNotInstalled(actionEvent.project!!)
                } else {
                    val toolsVersion = DotnetEfVersion.parse(efCoreDefinition.version)!!
                    ApplicationManager.getApplication().invokeLater {
                        ready(actionEvent, toolsVersion)
                    }
                }
            }
        })
    }

    protected abstract fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion)

    private fun getEfCoreRiderModel(actionEvent: AnActionEvent): RiderEfCoreModel {
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

    private fun notifyDotnetEfIsNotInstalled(intellijProject: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
            .createNotification("EF Core are required to execute this action", NotificationType.ERROR)
            .addAction(InstallDotnetEfAction())
            .notify(intellijProject)
    }

    data class DialogBuildParameters(
        val model: RiderEfCoreModel,
        val currentDotnetProjectName: String?
    )
}