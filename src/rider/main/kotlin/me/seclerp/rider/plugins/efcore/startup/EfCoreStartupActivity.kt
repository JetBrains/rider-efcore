package me.seclerp.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.rd.EfToolDefinition
import me.seclerp.rider.plugins.efcore.rd.ToolKind
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel

class EfCoreStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(intellijProject: Project) {
        ApplicationManager.getApplication().invokeAndWait {
            val currentValue = intellijProject.solution.riderEfCoreModel.efToolsDefinition.valueOrNull

            if (currentValue != null) {
                processToolDefinition(currentValue, intellijProject)
            }

            intellijProject.solution.riderEfCoreModel.efToolsDefinition.advise(intellijProject.lifetime) {
                processToolDefinition(it, intellijProject)
            }
        }
    }

    private fun processToolDefinition(toolDefinition: EfToolDefinition, intellijProject: Project) {
        if (toolDefinition.toolKind == ToolKind.None) {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification("EF Core tools are not installed", "These tools are required to execute EF Core commands", NotificationType.WARNING)
                .addAction(InstallDotnetEfAction())
                .notify(intellijProject)
        }
    }
}