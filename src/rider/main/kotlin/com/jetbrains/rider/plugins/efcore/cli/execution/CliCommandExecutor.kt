package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner
import org.jetbrains.plugins.terminal.TerminalProjectOptionsProvider

abstract class CliCommandExecutor(
    protected val intellijProject: Project
) {
    companion object {
        val logger = logger<CliCommandExecutor>()
    }

    suspend fun execute(command: CliCommand): CliCommandResult? {
        try {
            return doExecute(command)
        }
        catch (t: Throwable) {
            logger.error(t)
            return null
        }
        finally {
            refreshSolution()
        }
    }

    protected abstract suspend fun doExecute(command: CliCommand): CliCommandResult?

    private fun refreshSolution() {
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
    }

    /**
     * On Linux we need to specify shell explicitly, as command may be executed in the wrong one,
     * e.g. in Bash instead of Zsh.
     */
    protected fun wrapWithShell(command: GeneralCommandLine): GeneralCommandLine {
        if (!SystemInfo.isLinux) return command

        logger.info("Linux operating system detected, the command will be patched with the shell executable.")
        logger.info("Initial command: ${command.commandLineString}")

        val shellPath = TerminalProjectOptionsProvider.getInstance(intellijProject).shellPath
        val shellCommand = LocalTerminalDirectRunner.convertShellPathToCommand(shellPath)

        return command.apply {
            val trueExePath = exePath
            exePath = shellCommand.first()
            parametersList.addAt(0, trueExePath)
            parametersList.addAt(0, ParametersList.join(shellCommand.drop(1)))
            logger.info("Patched command: $commandLineString")
        }
    }
}