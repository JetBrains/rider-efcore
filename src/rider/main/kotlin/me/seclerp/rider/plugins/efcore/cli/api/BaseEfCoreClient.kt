package me.seclerp.rider.plugins.efcore.cli.api

import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandBuilder
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions

abstract class BaseEfCoreClient {
    protected fun createCommand(command: String, commonOptions: CommonOptions): CliCommand {
        return createCommand(command, commonOptions) { }
    }

    protected fun createCommand(command: String, commonOptions: CommonOptions, customOptionsApplier: CliCommandBuilder.() -> Unit): CliCommand {
        val commandBuilder = CliCommandBuilder(command, commonOptions)
        customOptionsApplier(commandBuilder)

        return commandBuilder.build()
    }
}