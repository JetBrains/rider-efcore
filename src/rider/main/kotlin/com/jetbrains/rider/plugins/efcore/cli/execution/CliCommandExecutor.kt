package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

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
}