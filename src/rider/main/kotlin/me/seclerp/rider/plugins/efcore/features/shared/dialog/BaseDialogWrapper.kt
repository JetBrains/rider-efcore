package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.ui.DialogWrapper
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

abstract class BaseDialogWrapper : DialogWrapper(true) {
    abstract fun generateCommand(): CliCommand

    open fun postCommandExecute(commandResult: CliCommandResult) {}

    protected open fun loadDialogState(dialogState: DialogsStateService.SpecificDialogState) {}

    protected open fun saveDialogState(dialogState: DialogsStateService.SpecificDialogState) {}
}