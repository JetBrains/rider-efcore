package com.jetbrains.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.jetbrains.rd.framework.impl.RdTask
import com.jetbrains.rd.platform.client.ProtocolProjectSession
import com.jetbrains.rd.protocol.SolutionExtListener
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel

@Suppress("UnstableApiUsage")
class EfCoreStartupListener : SolutionExtListener<RiderEfCoreModel> {
    override fun extensionCreated(lifetime: Lifetime, session: ProtocolProjectSession, model: RiderEfCoreModel) {
        model.onMissingEfCoreToolsDetected.set { _, unit ->
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(
                    EfCoreUiBundle.message("notification.title.ef.core.tools.are.not.installed"),
                    EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"),
                    NotificationType.WARNING
                )
                .addAction(InstallDotnetEfAction())
                .notify(session.project)

            RdTask.fromResult(unit)
        }
    }
}