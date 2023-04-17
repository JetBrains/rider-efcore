package me.seclerp.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.application
import com.jetbrains.rd.framework.impl.RdTask
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.KnownNotificationGroups
import me.seclerp.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel

// TODO: Remove obsolete API usage
class EfCoreStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(intellijProject: Project) {
        application.invokeAndWait {
            intellijProject.solution.riderEfCoreModel.onMissingEfCoreToolsDetected.set { _, unit ->
                NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup(KnownNotificationGroups.efCore)
                    .createNotification(
                        EfCoreUiBundle.message("notification.title.ef.core.tools.are.not.installed"),
                        EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"),
                        NotificationType.WARNING
                    )
                    .addAction(InstallDotnetEfAction())
                    .notify(intellijProject)

                RdTask.fromResult(unit)
            }
        }
    }
}