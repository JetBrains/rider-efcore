package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import me.seclerp.rider.plugins.efcore.clients.ManagementClient
import me.seclerp.rider.plugins.efcore.commands.executeCommandUnderProgress

class InstallEfCoreAction : AnAction("Fix") {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        executeCommandUnderProgress(
            actionEvent.project!!,
            "Installing EF Core global tools...",
            "EF Core global tools has been successfully installed",
            false
        ) {
            val managementClient = actionEvent.project!!.service<ManagementClient>()
            managementClient.installEfCoreTools()
        }
    }
}