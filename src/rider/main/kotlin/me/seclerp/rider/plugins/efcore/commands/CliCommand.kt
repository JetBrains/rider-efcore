package me.seclerp.rider.plugins.efcore.commands

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import java.io.IOException

class CliCommand(private val command: GeneralCommandLine) {
    fun execute(): CliCommandResult {
        return try {
            val result = ExecUtil.execAndGetOutput(command)

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