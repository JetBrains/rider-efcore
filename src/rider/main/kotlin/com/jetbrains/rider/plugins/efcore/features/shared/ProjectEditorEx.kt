package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jetbrains.rd.platform.util.TimeoutTracker
import com.jetbrains.rider.model.projectModelTasks
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.protocol.ProtocolManager
import com.jetbrains.rider.services.RiderProjectModelWaiter
import com.jetbrains.rider.util.idea.syncFromBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

private const val DEFAULT_TIMEOUT_SECONDS = 20L

private val logger = logger<Project>()

internal suspend fun Project.flushFileSystem(path: String,
                                             timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS)) {
    logger.info("Refreshing at path: $path")
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    VfsUtil.markDirtyAndRefresh(false, true, true, virtualFile)

    withContext(Dispatchers.Main) {
        logger.info("Wait for project model...")
        val timeoutTracker = TimeoutTracker(timeout)
        RiderProjectModelWaiter.waitForProjectModelReadySuspending(this@flushFileSystem, timeoutTracker)

        this@flushFileSystem.waitForSyncPoints()
    }
}

@Suppress("UnstableApiUsage")
internal fun Project.waitForSyncPoints(timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS)) {
    if (ProtocolManager.isResharperBackendDisabled())
        return

    logger.info("Wait for R# sync points...")
    val timeoutTracker = TimeoutTracker(timeout)
    val tasks = this.solution.projectModelTasks

    var hasNotReachedSyncPoints = true
    while (hasNotReachedSyncPoints) {
        val notReachedSyncPoints = tasks.getNotReachedSyncPoints.syncFromBackend(Unit, this)!!
        hasNotReachedSyncPoints = notReachedSyncPoints.any {
            // Do not wait for HotspotSession termination, continue with the active session
            !it.startsWith("HotspotSession")
        }

        if (timeoutTracker.isExpired) {
            logger.info("Sync points were not ready in time: ${notReachedSyncPoints.joinToString("\n")}")
            break
        }

        IdeEventQueue.getInstance().flushQueue()
    }
}