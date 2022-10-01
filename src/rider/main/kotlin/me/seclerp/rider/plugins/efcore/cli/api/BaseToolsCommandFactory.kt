package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandBuilder
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions

abstract class BaseToolsCommandFactory(private val workingDirectory: String) {
    protected fun createCommand(command: String, commonOptions: CommonOptions): GeneralCommandLine {
        return createCommand(command, commonOptions) { }
    }

    protected fun createCommand(command: String, commonOptions: CommonOptions, customOptionsApplier: CliCommandBuilder.() -> Unit): GeneralCommandLine {
        val commandBuilder = CliCommandBuilder(workingDirectory, command, commonOptions)
        customOptionsApplier(commandBuilder)

        return commandBuilder.build()
    }
}