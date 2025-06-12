package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.SimpleMessageBusConnection
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class OpenMigrationFileService(
    private val intellijProject: Project,
    private val projectScope: CoroutineScope) {
    fun openMigrationFile(migrationsFolderPath: String, migrationName: String) {

        intellijProject.messageBus.connect(projectScope).apply {
            subscribe(VirtualFileManager.VFS_CHANGES,
                MigrationFileCreatedListener(migrationsFolderPath, migrationName, this))
        }
    }

    private inner class MigrationFileCreatedListener(
        private val migrationsFolderPath: String,
        private val migrationName: String,
        private val connection: SimpleMessageBusConnection,
    ) : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) {
            for (event in events) {
                if (event !is VFileCreateEvent)
                    continue

                val file = event.file ?: continue

                // In cases where the migrations folder was created with the migration
                if (file.isDirectory && VfsUtil.pathEqualsTo(file, migrationsFolderPath)) {
                    for (child in file.children) {
                        if (tryHandleMigrationFile(child))
                            return
                    }
                }

                if (tryHandleMigrationFile(file))
                    return
            }
        }

        private fun tryHandleMigrationFile(file: VirtualFile): Boolean {
            if (file.isFile && VfsUtil.pathEqualsTo(file.parent, migrationsFolderPath) && checkMigrationName(file.name)) {
                openFileInEditor(file)
                connection.disconnect()
                return true
            }

            return false
        }

        private fun checkMigrationName(fileName: String) =
            Regex("^\\d{14}_${migrationName}.cs$").matches(fileName)
    }

    private fun openFileInEditor(file: VirtualFile) =
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(intellijProject).openFile(file, true)
        }
}