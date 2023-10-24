package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.settings.EfCoreUiSettingsStateService

@Service(Service.Level.PROJECT)
class PreferredCommandExecutorProvider(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<PreferredCommandExecutorProvider>()
    }

    private val settingsStateService by lazy { EfCoreUiSettingsStateService.getInstance() }

    fun getExecutor(): CliCommandExecutor =
        when (settingsStateService.useTerminalExecution) {
            true -> TerminalCommandExecutor(intellijProject)
            false -> SilentCommandExecutor(intellijProject)
        }
}