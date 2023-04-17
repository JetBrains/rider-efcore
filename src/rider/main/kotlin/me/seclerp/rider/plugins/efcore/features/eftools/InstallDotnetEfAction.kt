package me.seclerp.rider.plugins.efcore.features.eftools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.cli.api.ManagementCommandFactory
import me.seclerp.rider.plugins.efcore.cli.execution.NotificationCommandResultProcessor
import me.seclerp.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider

class InstallDotnetEfAction : AnAction(EfCoreUiBundle.message("action.fix.text")) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project!!
        val executor = project.service<PreferredCommandExecutorProvider>().getExecutor()
        val command = project.service<ManagementCommandFactory>().installEfCoreTools()
        val processor = NotificationCommandResultProcessor(
            project,
            EfCoreUiBundle.message("ef.core.global.tools.have.been.successfully.installed"),
            false
        )

        executor.execute(command, processor)
    }
}