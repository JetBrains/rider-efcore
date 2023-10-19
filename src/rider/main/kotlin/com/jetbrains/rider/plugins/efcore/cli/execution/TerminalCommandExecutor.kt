package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.process.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.withUiContext
import com.intellij.openapi.util.Key
import com.intellij.terminal.TerminalExecutionConsole
import com.jetbrains.rider.run.TerminalProcessHandler
import kotlinx.coroutines.*

class TerminalCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {
    private val logger = logger<TerminalCommandExecutor>()
    private val toolWindowProvider by lazy { EfCoreConsoleToolWindowProvider.getInstance(intellijProject) }

    override suspend fun doExecute(command: CliCommand): CliCommandResult? {
        return try {
            val processCompletion = CompletableDeferred<CliCommandResult>()
            val processHandler = createProcessHandler(command, processCompletion)
            withUiContext {
                val consoleView = TerminalExecutionConsole(intellijProject, processHandler)
                toolWindowProvider.createTab(command, consoleView)
            }
            logger.info("Starting process '${command.commandLine.commandLineString}'")
            processHandler.startNotify()
            processCompletion.await()
        } catch (cancellation: CancellationException) {
            null
        }
    }

    private fun createProcessHandler(command: CliCommand,
                                     completion: CompletableDeferred<CliCommandResult>): TerminalProcessHandler =
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
                            logger.info("${command.presentationInfo.name} [$outputType]: $trimmed")
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

                    completion.complete(result)
                }

                override fun processNotStarted() {
                    logger.error("Process wasn't started")

                    completion.cancel()
                }
            })
        }
}
