package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand

abstract class BaseDialogWrapper : DialogWrapper(true) {
    abstract fun generateCommand(): CliCommand

    open fun postCommandExecute(commandResult: CliCommandResult) {}
}