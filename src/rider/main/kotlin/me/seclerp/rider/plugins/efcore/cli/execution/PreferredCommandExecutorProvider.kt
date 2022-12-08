package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.settings.EfCoreUiSettingsStateService

@Service
class PreferredCommandExecutorProvider(private val intellijProject: Project) {
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()

    fun getExecutor(): CliCommandExecutor =
        when (settingsStateService.useTerminalExecution) {
            false -> SilentCommandExecutor(intellijProject)
            true -> TerminalCommandExecutor(intellijProject)
        }
}