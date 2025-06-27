package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.jetbrains.rd.util.reactive.Signal
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class FileEventsListenerService(intellijProject: Project, projectScope: CoroutineScope) {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<FileEventsListenerService>()
    }

    val fileCreated = Signal<VirtualFile>()

    init {
        intellijProject.messageBus.connect(projectScope).subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) = events.forEach { processEvent(it) }
            }
        )
    }

    private fun processEvent(event: VFileEvent) {
        var file = event.file

        if (event !is VFileCreateEvent || file == null)
            return

        if (file.isFile)
            fileCreated.fire(file)
        else {
            // Handle the case where one or more directories were created as part of the add migration command
            // In this case, only one event will be fired - for the newly created directory lowest in the hierarchy
            while (file != null && file.isDirectory && file.children.count() == 1) {
                file = file.children.firstOrNull()
            }
            file?.children?.forEach {
                fileCreated.fire(it)
            }
        }
    }
}