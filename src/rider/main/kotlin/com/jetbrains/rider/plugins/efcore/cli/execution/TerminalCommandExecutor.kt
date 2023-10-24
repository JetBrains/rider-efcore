package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.process.*
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.terminal.TerminalExecutionConsole
import com.intellij.util.application
import com.jetbrains.rider.run.TerminalProcessHandler

class TerminalCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {
    private val logger = logger<TerminalCommandExecutor>()
    private val toolWindowProvider by lazy { EfCoreConsoleToolWindowProvider.getInstance(intellijProject) }

    override fun execute(command: DotnetCommand, resultProcessor: CliCommandResultProcessor?) {
        val processHandler = createProcessHandler(command, resultProcessor) {
            execute(command, resultProcessor)
        }
        val consoleView = createConsoleView(processHandler)
        toolWindowProvider.createTab(command, consoleView)

        logger.info("Starting process '${command.commandLine.commandLineString}'")
        processHandler.startNotify()
    }

    private fun createConsoleView(processHandler: ProcessHandler): ConsoleView {
        application.assertIsDispatchThread()

        return TerminalExecutionConsole(intellijProject, processHandler)
    }

    private fun createProcessHandler(command: DotnetCommand,
                                     resultProcessor: CliCommandResultProcessor?,
                                     retryAction: () -> Unit): TerminalProcessHandler =
        TerminalProcessHandler(intellijProject, command.commandLine, command.commandLine.commandLineString).apply {
            addProcessListener(object : ProcessAdapter() {
                private val ansiDecoder = AnsiEscapeDecoder()
                private val outputBuilder = StringBuilder()
                private val errorBuilder = StringBuilder()
                private val logger = logger<TerminalCommandExecutor>()

                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    val text = event.text
                    ansiDecoder.escapeText(text, outputType) { chunk, _ ->
                        val trimmed = chunk.trim()
                        if (trimmed.isNotEmpty()) {
                            logger.info("${command.presentableName} [$outputType]: $trimmed")
                            when (outputType) {
                                ProcessOutputTypes.STDOUT -> outputBuilder.append(trimmed)
                                ProcessOutputTypes.STDERR -> errorBuilder.append(trimmed)
                            }
                        }
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    val result = CliCommandResult(
                        command.commandLine.commandLineString,
                        event.exitCode,
                        outputBuilder.toString(),
                        exitCode == 0,
                        errorBuilder.toString()
                    )

                    resultProcessor?.process(result, retryAction)
                }
            })
        }
}
