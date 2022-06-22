package me.seclerp.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.application
import com.jetbrains.rd.framework.impl.RdTask
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel

class EfCoreStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(intellijProject: Project) {
        application.invokeAndWait {
            intellijProject.solution.riderEfCoreModel.onMissingEfCoreToolsDetected.set { _, unit ->
                NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup(KnownNotificationGroups.efCore)
                    .createNotification(
                        "EF Core tools are not installed",
                        "These tools are required to execute EF Core commands",
                        NotificationType.WARNING
                    )
                    .addAction(InstallDotnetEfAction())
                    .notify(intellijProject)

                RdTask.fromResult(unit)
            }
        }
    }
}