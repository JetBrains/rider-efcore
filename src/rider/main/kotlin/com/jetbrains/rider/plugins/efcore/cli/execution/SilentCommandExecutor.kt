package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import java.io.IOException

class SilentCommandExecutor(
    intellijProject: Project
) : CliCommandExecutor(intellijProject) {
    override fun execute(command: DotnetCommand, resultProcessor: CliCommandResultProcessor?) {
        runBackgroundableTask(EfCoreUiBundle.message("progress.title.executing.ef.core.command"), intellijProject, false) {
            try {
                val executionResult = ExecUtil.execAndGetOutput(command.commandLine)
                val output = executionResult.stdout
                val error = executionResult.stderr
                val exitCode = executionResult.exitCode

                resultProcessor?.process(
                    CliCommandResult(
                        command.commandLine.commandLineString,
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

                resultProcessor?.process(CliCommandResult(command.commandLine.commandLineString, -1, e.toString(), false)) {
                    execute(command, resultProcessor)
                }
            }
        }
    }
}