package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.indeterminateStep
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.features.shared.TryCommandAgainAction
import java.io.IOException

@Suppress("UnstableApiUsage", "OPT_IN_USAGE")
class SilentCommandExecutor(
    intellijProject: Project
) : CliCommandExecutor(intellijProject) {
    override suspend fun doExecute(command: CliCommand): CliCommandResult {
        return withBackgroundProgress(intellijProject, EfCoreUiBundle.message("progress.title.executing.ef.core.command"), false) {
            indeterminateStep {
                try {
                    val commandLine = wrapWithShell(command.commandLine)
                    val executionResult = ExecUtil.execAndGetOutput(commandLine)
                    val output = executionResult.stdout
                    val error = executionResult.stderr
                    val exitCode = executionResult.exitCode
                    val result = CliCommandResult(
                        command.commandLine.commandLineString,
                        exitCode,
                        output,
                        exitCode == 0,
                        error
                    )

                    notify(command, result)
                    result
                } catch (e: IOException) {
                    e.printStackTrace()

                    val result = CliCommandResult(command.commandLine.commandLineString, -1, e.toString(), false)
                    notify(command, result)
                    result
                }
            }
        }
    }

    private fun notify(command: CliCommand, result: CliCommandResult) {
        if (result.succeeded) {
            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(command.presentationInfo.onSuccessNotification, NotificationType.INFORMATION)
                .notify(intellijProject)
        } else {
            val errorText = buildString {
                append(EfCoreUiBundle.message("initial.command", result.command))

                if (result.output.trim().isNotEmpty())
                    append("\n\n${EfCoreUiBundle.message("output", result.output)}")

                if (result.error?.trim()?.isNotEmpty() == true)
                    append("\n\n${EfCoreUiBundle.message("error", result.error)}")

                append("\n\n${EfCoreUiBundle.message("exit.code", result.exitCode)}")
            }

            NotificationGroupManager.getInstance().getNotificationGroup(KnownNotificationGroups.efCore)
                .createNotification(
                    EfCoreUiBundle.message("notification.title.ef.core.command.failed"),
                    errorText,
                    NotificationType.ERROR
                )
                .addAction(TryCommandAgainAction { execute(command) })
                .notify(intellijProject)
        }
    }
}