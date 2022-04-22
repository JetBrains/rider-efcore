package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import java.nio.charset.Charset

@Service
class ManagementClient : BaseEfCoreClient() {
    fun getEfCoreVersion(): DotnetEfVersion? {
        val generalCommandLine =
            GeneralCommandLine("dotnet", "ef", "--version")
                .withCharset(Charset.forName("UTF-8"))

        val command = CliCommand(generalCommandLine)
        val result = command.execute()
        val match = SEMVER_REGEX.find(result.output) ?: return null

        return if (result.succeeded)
            DotnetEfVersion.fromStrings(match.groups[1]!!.value, match.groups[2]!!.value, match.groups[3]!!.value)
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

    companion object {
        val SEMVER_REGEX = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([\\dA-Za-z-]+(?:\\.[\\dA-Za-z-]+)*))?(?:\\+[\\dA-Za-z-]+)?")
    }
}