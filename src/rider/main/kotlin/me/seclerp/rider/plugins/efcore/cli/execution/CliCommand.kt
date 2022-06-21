package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import java.io.IOException

class CliCommand(private val command: GeneralCommandLine) {
    val workingDirectory: String
    val commandText: String

    init {
        workingDirectory = command.workDirectory.toString()
        commandText = command.commandLineString
    }

    fun execute(): CliCommandResult {
        return try {
            val configuredCommand = command
                .withEnvironment("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")
                .withEnvironment("DOTNET_NOLOGO", "true")

            val result = ExecUtil.execAndGetOutput(configuredCommand)
            val output = result.stdout
            val error = result.stderr
            val exitCode = result.exitCode

            CliCommandResult(command.commandLineString, exitCode, output, exitCode == 0, error)
        } catch(e: IOException) {
            e.printStackTrace()

            CliCommandResult(command.commandLineString, -1, e.toString(), false)
        }
    }
}