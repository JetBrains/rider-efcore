package me.seclerp.rider.plugins.efcore.clients

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.commands.CliCommand
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import java.nio.charset.Charset

@Service
class ManagementClient : BaseEfCoreClient() {
    fun getEfCoreVersion(): String? {
        val generalCommandLine =
            GeneralCommandLine("dotnet", "ef", "--version")
                .withCharset(Charset.forName("UTF-8"))

        val command = CliCommand(generalCommandLine)
        val result = command.execute()

        return if (result.succeeded)
            result.output
        else
            null
    }

    fun installEfCoreTools(): CliCommandResult {
        val generalCommandLine =
            GeneralCommandLine("dotnet", "tool", "install", "--global", "dotnet-ef")
                .withCharset(Charset.forName("UTF-8"))

        val command = CliCommand(generalCommandLine)
        return command.execute()
    }
}