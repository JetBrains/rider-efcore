package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.terminal.TerminalExecutionConsole
import com.jetbrains.rd.platform.util.getLogger
import com.jetbrains.rider.run.TerminalProcessHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val logger = getLogger<TerminalCommandExecutor>()

class TerminalCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {

    private val toolWindowProvider by lazy { EfCoreConsoleToolWindowProvider.getInstance(intellijProject) }

    override suspend fun doExecute(command: CliCommand): CliCommandResult {
      val processCompletion = CompletableDeferred<CliCommandResult>()
      val processHandler = createProcessHandler(command, processCompletion)
      withContext(Dispatchers.Main) {
          val consoleView = TerminalExecutionConsole(intellijProject, processHandler)
          toolWindowProvider.createTab(command, consoleView)
      }
      logger.info("Starting process '${command.commandLine.commandLineString}'")
      processHandler.startNotify()
      return processCompletion.await()
    }

    /**
     * Creates the process with its stderr in the same pty as its stdout. The terminal console then gets one stream, in the order
     * that the process writes it.
     */
    private fun createProcessHandler(command: CliCommand,
                                     completion: CompletableDeferred<CliCommandResult>): TerminalProcessHandler {
        val commandLine = command.commandLine.withRedirectErrorStream(true)
        return TerminalProcessHandler(intellijProject, commandLine, commandLine.commandLineString).apply {
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
                            when {
                                ProcessOutputType.isStdout(outputType) -> outputBuilder.append(trimmed)
                                ProcessOutputType.isStderr(outputType) -> errorBuilder.append(trimmed)
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
}
