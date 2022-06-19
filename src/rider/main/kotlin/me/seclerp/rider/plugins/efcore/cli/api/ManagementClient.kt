package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import java.nio.charset.Charset

@Service
class ManagementClient : BaseEfCoreClient() {
    fun installEfCoreTools(): CliCommandResult {
        val generalCommandLine =
            GeneralCommandLine("dotnet", "tool", "install", "--global", "dotnet-ef")
                .withCharset(Charset.forName("UTF-8"))

        val command = CliCommand(generalCommandLine)
        return command.execute()
    }
}