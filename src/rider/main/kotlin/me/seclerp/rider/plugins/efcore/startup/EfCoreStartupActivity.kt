package me.seclerp.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.jetbrains.rd.framework.impl.RpcTimeouts
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.cli.api.ManagementClient
import me.seclerp.rider.plugins.efcore.rd.StartupProjectInfo
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel

class EfCoreStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(intellijProject: Project) {
        intellijProject.solution.riderEfCoreModel.efToolsVersion.advise(intellijProject.lifetime) {
            if (it == "") {
                NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                    .createNotification("EF Core tools are not installed", "These tools are required to execute EF Core commands", NotificationType.WARNING)
                    .addAction(InstallDotnetEfAction())
                    .notify(intellijProject)
            }
        }

        var efCoreStartupProjects: List<StartupProjectInfo>? = null
            ApplicationManager.getApplication().invokeAndWait {
                efCoreStartupProjects = intellijProject.solution.riderEfCoreModel.getAvailableStartupProjects.sync(Unit, RpcTimeouts.longRunning)
            }

        if (efCoreStartupProjects?.isEmpty() != false) {
            return
        }

        val efCoreChecker = intellijProject.service<ManagementClient>()
        val version = efCoreChecker.getEfCoreVersion()

        if (version == null) {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification("EF Core tools are not installed", "These tools are required to execute EF Core commands", NotificationType.WARNING)
                .addAction(InstallDotnetEfAction())
                .notify(intellijProject)
        }
    }
}