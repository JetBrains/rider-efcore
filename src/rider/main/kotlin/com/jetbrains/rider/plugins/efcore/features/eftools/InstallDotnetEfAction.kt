package com.jetbrains.rider.plugins.efcore.features.eftools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.rd.util.launchOnUi
import com.intellij.openapi.rd.util.lifetime
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.indeterminateStep
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.execution.NotificationCommandResultProcessor
import com.jetbrains.rider.plugins.efcore.cli.execution.PreferredCommandExecutorProvider
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution

class InstallDotnetEfAction : AnAction(EfCoreUiBundle.message("action.install.text")) {
  override fun actionPerformed(actionEvent: AnActionEvent) {
    val project = actionEvent.project!!
    val executor = PreferredCommandExecutorProvider.getInstance(project).getExecutor()
    val command = ManagementCommandFactory.getInstance(project).installEfCoreTools()
    val processor = NotificationCommandResultProcessor(
      project,
      EfCoreUiBundle.message("ef.core.global.tools.have.been.successfully.installed"),
      false
    ).withPostExecuted {
      if (!it.succeeded) return@withPostExecuted
      project.lifetime.launchOnUi {
        withBackgroundProgress(project, EfCoreUiBundle.message("progress.title.updating.tools.cache")) {
          indeterminateStep {
            project.solution.riderEfCoreModel.refreshDotNetToolsCache.startSuspending(project.lifetime, Unit)
          }
        }
      }
    }

    executor.execute(command, processor)
  }
}