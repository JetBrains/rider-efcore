package me.seclerp.rider.plugins.efcore.clients

import me.seclerp.rider.plugins.efcore.commands.CliCommand
import me.seclerp.rider.plugins.efcore.commands.CliCommandBuilder
import me.seclerp.rider.plugins.efcore.commands.CommonOptions

abstract class BaseEfCoreClient {
    protected fun createCommand(command: String, commonOptions: CommonOptions): CliCommand {
        return createCommand(command, commonOptions) { it }
    }

    protected fun createCommand(command: String, commonOptions: CommonOptions, customOptionsApplier: (CliCommandBuilder) -> CliCommandBuilder): CliCommand {
        val commandBuilder = customOptionsApplier(CliCommandBuilder(command, commonOptions))

        return commandBuilder.build()
    }
}