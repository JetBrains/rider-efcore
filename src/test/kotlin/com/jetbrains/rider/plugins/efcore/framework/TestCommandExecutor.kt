package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandExecutor
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResultProcessor

@Suppress("HardCodedStringLiteral")
class TestCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {
    private val logger = logger<TestCommandExecutor>()

    override fun execute(command: GeneralCommandLine, resultProcessor: CliCommandResultProcessor?) {
        OSProcessHandler(command).apply {
            addProcessListener(LoggerProcessListener(command, resultProcessor) {
                execute(command, resultProcessor)
            })
            logger.info("Starting process '${command.commandLineString}'")
            startNotify()
            waitFor()
        }
    }

    inner class LoggerProcessListener(
        private val command: GeneralCommandLine,
        private val resultProcessor: CliCommandResultProcessor?,
        private val retryAction: () -> Unit
    ) : ProcessAdapter() {
        private val outputBuilder = StringBuilder()
        private val errorBuilder = StringBuilder()
        private val logger = logger<LoggerProcessListener>()
        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
            val text = event.text.trim()
            logger.info("${command.exePath} [$outputType]: ${text}")
            outputBuilder.append(text)
        }

        override fun processTerminated(event: ProcessEvent) {
            logger.info(EfCoreUiBundle.message("process.terminated.with.exit.code", event.exitCode))

            val result = CliCommandResult(
                command.commandLineString,
                event.exitCode,
                outputBuilder.toString(),
                event.exitCode == 0,
                errorBuilder.toString()
            )

            resultProcessor?.process(result, retryAction)
        }
    }
}