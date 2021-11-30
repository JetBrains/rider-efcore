package me.seclerp.rider.plugins.efcore.clients

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.commands.CliCommand
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult

@Service
class ManagementClient : BaseEfCoreClient() {
    fun getEfCoreVersion(): String? {
        val command = CliCommand("dotnet ef --version")
        val result = command.execute()

        return if (result.succeeded)
            result.output
        else
            null
    }

    fun installEfCoreTools(): CliCommandResult {
        val command = CliCommand("dotnet tool install --global dotnet-ef")
        return command.execute()
    }
}