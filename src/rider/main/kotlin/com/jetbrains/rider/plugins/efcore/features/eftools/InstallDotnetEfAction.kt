package com.jetbrains.rider.plugins.efcore.features.eftools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.rd.util.launchBackground
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider

class InstallDotnetEfAction : AnAction(EfCoreUiBundle.message("action.install.text")) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val executor = PreferredCommandExecutorProvider.getInstance(project).getExecutor()
        val command = ManagementCommandFactory.getInstance(project).installEfCoreTools()
        project.lifetime.launchBackground {
            executor.execute(command)
        }
    }
}