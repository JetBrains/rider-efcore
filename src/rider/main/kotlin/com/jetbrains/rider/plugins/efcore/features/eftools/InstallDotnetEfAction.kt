package com.jetbrains.rider.plugins.efcore.features.eftools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.execution.NotificationCommandResultProcessor
import com.jetbrains.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider

class InstallDotnetEfAction : AnAction(EfCoreUiBundle.message("action.install.text")) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project!!
        val executor = PreferredCommandExecutorProvider.getInstance(project).getExecutor()
        val command = ManagementCommandFactory.getInstance(project).installEfCoreTools()
        val processor = NotificationCommandResultProcessor(
            project,
            EfCoreUiBundle.message("ef.core.global.tools.have.been.successfully.installed"),
            false
        )

        executor.execute(command, processor)
    }
}