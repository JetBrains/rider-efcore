package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jetbrains.rd.platform.util.TimeoutTracker
import com.jetbrains.rider.model.projectModelTasks
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.services.RiderBackendWaiter
import com.jetbrains.rider.services.RiderProjectModelWaiter
import com.jetbrains.rider.util.idea.syncFromBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.text.startsWith

@Suppress("UnstableApiUsage")
internal suspend fun Project.waitForFiles(folderPath: String) {
    val migrationDirVF = LocalFileSystem.getInstance().refreshAndFindFileByPath(folderPath)
    VfsUtil.markDirtyAndRefresh(false, true, true, migrationDirVF)

    withContext(Dispatchers.Main) {
        RiderBackendWaiter.waitBackendSuspending(this@waitForFiles)
        val timeoutTracker = TimeoutTracker(java.time.Duration.ofSeconds(20))
        RiderProjectModelWaiter.waitForProjectModelReadySuspending(this@waitForFiles, timeoutTracker)

        val tasks = this@waitForFiles.solution.projectModelTasks
        var hasNotReachedSyncPoints = true
        while (hasNotReachedSyncPoints) {
            val notReachedSyncPoints = tasks.getNotReachedSyncPoints.syncFromBackend(Unit, this@waitForFiles)!!
            hasNotReachedSyncPoints = notReachedSyncPoints.any {
                // Do not wait for HotspotSession termination, continue tests with active session
                !it.startsWith("HotspotSession")
            }

            IdeEventQueue.getInstance().flushQueue()
        }
    }
}