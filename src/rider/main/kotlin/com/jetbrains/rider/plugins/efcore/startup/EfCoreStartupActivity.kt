package com.jetbrains.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.application
import com.jetbrains.rd.framework.impl.RdTask
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel

class EfCoreStartupActivity : ProjectActivity, DumbAware {
    override suspend fun execute(project: Project) {
        application.invokeAndWait {
            project.solution.riderEfCoreModel.onMissingEfCoreToolsDetected.set { _, unit ->
                NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup(KnownNotificationGroups.efCore)
                    .createNotification(
                            EfCoreUiBundle.message("notification.title.ef.core.tools.are.not.installed"),
                            EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"),
                            NotificationType.WARNING
                    )
                    .addAction(InstallDotnetEfAction())
                    .notify(project)

                RdTask.fromResult(unit)
            }
        }
    }
}