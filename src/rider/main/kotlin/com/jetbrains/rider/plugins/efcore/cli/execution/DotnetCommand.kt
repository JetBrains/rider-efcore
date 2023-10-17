package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.NlsContexts

open class DotnetCommand(
    val dotnetPath: String,
    val commandLine: GeneralCommandLine,
    @NlsContexts.TabTitle
    val presentableName: String
)