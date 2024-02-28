package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandExecutor
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

@Suppress("HardCodedStringLiteral")
class TestCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {
    private val logger = logger<TestCommandExecutor>()

    override suspend fun doExecute(command: CliCommand): CliCommandResult? {
        return try {
            val processCompletion = CompletableDeferred<CliCommandResult>()
            val processHandler = createProcessHandler(command, processCompletion)
            logger.info("Starting process '${command.commandLine.commandLineString}'")
            processHandler.startNotify()
            processCompletion.await()
        } catch (cancellation: CancellationException) {
            null
        }
    }

    private fun createProcessHandler(command: CliCommand,
                                     completion: CompletableDeferred<CliCommandResult>): ProcessHandler =
        OSProcessHandler(command.commandLine).apply {
            addProcessListener(object : ProcessAdapter() {
                private val outputBuilder = StringBuilder()
                private val errorBuilder = StringBuilder()
                private val logger = logger<TestCommandExecutor>()
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    val text = event.text.trim()
                    logger.info("${command.commandLine.exePath} [$outputType]: ${text}")
                    outputBuilder.append(text)
                }

                override fun processTerminated(event: ProcessEvent) {
                    logger.info(EfCoreUiBundle.message("process.terminated.with.exit.code", event.exitCode))

                    val result = CliCommandResult(
                        command.commandLine.commandLineString,
                        event.exitCode,
                        outputBuilder.toString(),
                        event.exitCode == 0,
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