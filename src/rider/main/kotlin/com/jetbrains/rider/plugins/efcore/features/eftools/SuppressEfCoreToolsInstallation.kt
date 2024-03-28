package com.jetbrains.rider.plugins.efcore.features.eftools

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.state.CommonOptionsStateService

class SuppressEfCoreToolsInstallation(private val notification : Notification) : AnAction(EfCoreUiBundle.message("action.install.ignore")) {
    private val commonOptionsStateService by lazy { CommonOptionsStateService.getInstance() }
    override fun actionPerformed(actionEvent: AnActionEvent) {
        commonOptionsStateService.setProjectToolsInstallationSupressed(true)
        notification.hideBalloon()
    }
}