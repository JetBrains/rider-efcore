package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import com.intellij.util.io.BaseOutputReader
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import org.jetbrains.annotations.NonNls
import java.util.logging.Logger

class TerminalCommandExecutor(intellijProject: Project) : CliCommandExecutor(intellijProject) {
    private val consoleViewProvider: ConsoleViewProvider = intellijProject.service()
    private val logger = logger<TerminalCommandExecutor>()

    override fun execute(command: GeneralCommandLine, resultProcessor: CliCommandResultProcessor?) {
        val consoleView = getConsoleView()
        val processHandler = ConsoleViewProcessHandler(command, consoleView, resultProcessor) {
            execute(command, resultProcessor)
        }

        consoleView.clear()
        consoleView.attachToProcess(processHandler)
        consoleViewProvider.toolWindow.value.activate(null)

        logger.info("Starting process '${command.commandLineString}'")
        processHandler.startNotify()
    }

    private fun getConsoleView(): ConsoleView {
        application.assertIsDispatchThread()

        return consoleViewProvider.consoleView.value
    }

    class ConsoleViewProcessHandler(
        private val command: GeneralCommandLine,
        private val consoleView: ConsoleView,
        private val resultProcessor: CliCommandResultProcessor?,
        private val retryAction: () -> Unit
    ) : OSProcessHandler(command) {
        private val outputBuilder = StringBuilder()
        private val errorBuilder = StringBuilder()
        private val logger = logger<TerminalCommandExecutor>()

        override fun readerOptions() =
            BaseOutputReader.Options.forMostlySilentProcess()

        override fun notifyTextAvailable(text: String, outputType: Key<*>) {
            val title = ConsoleViewProvider.TAB_NAME
            logger.info("$title [$outputType]: $text")

            if (outputType === ProcessOutputTypes.STDOUT) {
                outputBuilder.append(text)
            } else if (outputType === ProcessOutputTypes.STDERR) {
                errorBuilder.append(text)
            }

            super.notifyTextAvailable(text, outputType)
        }

        override fun notifyProcessTerminated(exitCode: Int) {
            consoleView.print(EfCoreUiBundle.message("process.terminated.with.exit.code", exitCode), ConsoleViewContentType.SYSTEM_OUTPUT)

            val result = CliCommandResult(
                command.commandLineString,
                exitCode,
                outputBuilder.toString(),
                exitCode == 0,
                errorBuilder.toString()
            )

            resultProcessor?.process(result, retryAction)

            super.notifyProcessTerminated(exitCode)
        }
    }

    @Suppress("UnstableApiUsage")
    @Service(Service.Level.PROJECT)
    class ConsoleViewProvider(private val intellijProject: Project) {
        companion object {
            private val TOOL_WINDOW_TASK_ID = EfCoreUiBundle.message("tab.task.ef.core")
            val TAB_NAME = EfCoreUiBundle.message("tab.ef.core.command")
        }

        val toolWindow = lazy {
            ToolWindowManager.getInstance(intellijProject).registerToolWindow(
                RegisterToolWindowTask(TOOL_WINDOW_TASK_ID, ToolWindowAnchor.BOTTOM, canCloseContent = false)
            )
        }

        val consoleView = lazy {
            TextConsoleBuilderFactory.getInstance().createBuilder(intellijProject).console.apply {
                toolWindow.value.contentManager.factory.createContent(component, TAB_NAME, true).apply {
                    toolWindow.value.contentManager.addContent(this)
                }
            }
        }
    }
}
