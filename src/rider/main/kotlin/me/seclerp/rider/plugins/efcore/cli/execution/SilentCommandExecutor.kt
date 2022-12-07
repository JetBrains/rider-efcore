package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import java.io.IOException

class SilentCommandExecutor(
    intellijProject: Project
) : CliCommandExecutor(intellijProject) {
    override fun execute(command: GeneralCommandLine, resultProcessor: CliCommandResultProcessor?) {
        runBackgroundableTask("Executing EF Core command...", intellijProject, false) {
            try {
                val executionResult = ExecUtil.execAndGetOutput(command)
                val output = executionResult.stdout
                val error = executionResult.stderr
                val exitCode = executionResult.exitCode

                resultProcessor?.process(
                    CliCommandResult(
                        command.commandLineString,
                        exitCode,
                        output,
                        exitCode == 0,
                        error
                    )
                ) {
                    execute(command, resultProcessor)
                }
            } catch (e: IOException) {
                e.printStackTrace()

                resultProcessor?.process(CliCommandResult(command.commandLineString, -1, e.toString(), false)) {
                    execute(command, resultProcessor)
                }
            }
        }
    }
}