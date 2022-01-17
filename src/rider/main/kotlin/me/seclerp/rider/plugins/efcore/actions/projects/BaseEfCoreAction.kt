package me.seclerp.rider.plugins.efcore.actions.projects

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
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.actions.getDotnetProjectName
import me.seclerp.rider.plugins.efcore.actions.isLoadedProjectFile
import me.seclerp.rider.plugins.efcore.actions.notifications.InstallEfCoreAction
import me.seclerp.rider.plugins.efcore.clients.ManagementClient
import me.seclerp.rider.plugins.efcore.dialogs.BaseEfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.CommonOptions
import me.seclerp.rider.plugins.efcore.models.EfCoreVersion

abstract class BaseEfCoreAction : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isLoadedProjectFile()
    }

    fun getCommonOptions(dialog: BaseEfCoreDialogWrapper): CommonOptions =
        CommonOptions(
            dialog.migrationsProject!!.data.fullPath,
            dialog.startupProject!!.data.fullPath,
            dialog.dbContext!!.data,
            dialog.buildConfiguration!!.displayName,
            dialog.targetFramework!!.data,
            dialog.noBuild
        )

    override fun actionPerformed(actionEvent: AnActionEvent) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(actionEvent.project, "Getting dotnet ef version...", false) {
            override fun run(progress: ProgressIndicator) {
                val efCoreVersion = actionEvent.project!!.getService<ManagementClient>().getEfCoreVersion()

                if (efCoreVersion == null) {
                    notifyDotnetEfIsNotInstalled(actionEvent.project!!)
                } else {
                    ApplicationManager.getApplication().invokeLater {
                        ready(actionEvent, efCoreVersion)
                    }
                }
            }
        })
    }

    protected abstract fun ready(actionEvent: AnActionEvent, efCoreVersion: EfCoreVersion)

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
            .addAction(InstallEfCoreAction())
            .notify(intellijProject)
    }

    data class DialogBuildParameters(
        val model: RiderEfCoreModel,
        val currentDotnetProjectName: String
    )
}