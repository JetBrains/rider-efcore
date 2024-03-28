package com.jetbrains.rider.plugins.efcore.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.client.ClientProjectSession
import com.jetbrains.rd.framework.impl.RdTask
import com.jetbrains.rd.protocol.SolutionExtListener
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.features.eftools.InstallDotnetEfAction
import com.jetbrains.rider.plugins.efcore.features.eftools.SuppressEfCoreToolsInstallation
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import com.jetbrains.rider.plugins.efcore.state.CommonOptionsStateService

@Suppress("UnstableApiUsage")
class EfCoreStartupListener : SolutionExtListener<RiderEfCoreModel> {
  private val commonOptionsStateService by lazy { CommonOptionsStateService.getInstance() }
  override fun extensionCreated(lifetime: Lifetime, session: ClientProjectSession, model: RiderEfCoreModel) {
    if(!commonOptionsStateService.getProjectToolsInstallationSupressed()) {
      model.onMissingEfCoreToolsDetected.set { _, unit ->
        NotificationGroupManager
          .getInstance()
          .getNotificationGroup(KnownNotificationGroups.efCore)
          .createNotification(
            EfCoreUiBundle.message("notification.title.ef.core.tools.required"),
            EfCoreUiBundle.message("notification.content.ef.core.tools.are.required.to.execute.this.action"),
            NotificationType.WARNING
          )
          .addAction(InstallDotnetEfAction())
          .apply{
            addAction(SuppressEfCoreToolsInstallation(this))
          }
          .notify(session.project)

        RdTask.fromResult(unit)
      }
    }
  }
}