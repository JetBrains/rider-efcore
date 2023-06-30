package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.settings.EfCoreUiSettingsStateService

@Service(Service.Level.PROJECT)
class PreferredCommandExecutorProvider(private val intellijProject: Project) {
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()

    fun getExecutor(): CliCommandExecutor =
        when (settingsStateService.useTerminalExecution) {
            false -> SilentCommandExecutor(intellijProject)
            true -> TerminalCommandExecutor(intellijProject)
        }
}