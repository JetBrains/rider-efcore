package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine

data class CliCommand(
    val dotnetPath: String,
    val commandLine: GeneralCommandLine,
    val presentationInfo: CliCommandPresentationInfo
)