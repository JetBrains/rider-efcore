package com.jetbrains.rider.plugins.efcore.v2

import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.rd.util.launchOnUi
import com.intellij.openapi.rd.util.lifetime
import com.intellij.openapi.rd.util.withBackgroundContext
import com.jetbrains.rider.plugins.efcore.cli.api.EfCoreCliCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.actionDotnetProjectFile
import com.jetbrains.rider.plugins.efcore.features.shared.statistics.CommandUsageCollector

internal abstract class CommandAction(private val type: CommandDialogType) : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isEfCoreActionContext()
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val intellijProject = actionEvent.project ?: return
        intellijProject.lifetime.launchOnUi {
            val version = fetchEfCoreVersion(intellijProject) ?: return@launchOnUi
            openDialog(actionEvent, version)
        }
    }

    private suspend fun openDialog(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project ?: return
        val dialog = CommandDialogFactory.getInstance(intellijProject).createDialog(type)

        if (dialog.showAndGet()) {
            val command = dialog.generateCommand()
            val cliCommand = EfCoreCliCommandFactory.getInstance(intellijProject).create(command, efCoreVersion)
            CommandUsageCollector.withCommandActivity(intellijProject, command) {
                withBackgroundContext {
                    executeCommand(cliCommand, intellijProject)
                }
            }
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    private fun AnActionEvent.isEfCoreActionContext() =
        isProjectsModeContext() || isSolutionModeContext()

    private fun AnActionEvent.isProjectsModeContext(): Boolean {
        // Check if action is executing through Solution view, otherwise VIRTUAL_FILE from below may point to wrong file from currently opened editor.
        if (uiKind != ActionUiKind.POPUP) return false
        // Check if solution is loaded
        if (project == null) return false
        // Fast check if action file extension is supported (F# or C# project)
        val extension = getData(PlatformDataKeys.VIRTUAL_FILE)?.extension ?: return false
        if (!isSupportedProjectExtension(extension)) return false
        // Check that currently opened project is loaded and known by backend
        if (actionDotnetProjectFile == null) return false
        return true
    }

    private fun AnActionEvent.isSolutionModeContext(): Boolean {
        val extension = getData(PlatformDataKeys.VIRTUAL_FILE)?.extension
        return when {
            uiKind == ActionUiKind.MAIN_MENU -> true
            uiKind == ActionUiKind.SEARCH_POPUP -> true
            uiKind == ActionUiKind.TOOLBAR -> true
            isFromContextMenu && extension == "sln" || extension == "slnf" -> true
            else -> false
        }
    }

    private fun isSupportedProjectExtension(projectFileExtension: String) =
        projectFileExtension == "csproj"
        || projectFileExtension == "fsproj"
}