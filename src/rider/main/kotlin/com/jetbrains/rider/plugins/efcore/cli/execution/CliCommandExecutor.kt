package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project

abstract class CliCommandExecutor(
    protected val intellijProject: Project
) {
    abstract fun execute(command: GeneralCommandLine, resultProcessor: CliCommandResultProcessor? = null)
}