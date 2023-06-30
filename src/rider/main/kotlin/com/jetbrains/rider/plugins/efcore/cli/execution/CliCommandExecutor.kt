package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.SaveAndSyncHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.rider.plugins.efcore.KnownNotificationGroups
import com.jetbrains.rider.plugins.efcore.features.shared.TryCommandAgainAction

abstract class CliCommandExecutor(
    protected val intellijProject: Project
) {
    abstract fun execute(command: GeneralCommandLine, resultProcessor: CliCommandResultProcessor? = null)
}