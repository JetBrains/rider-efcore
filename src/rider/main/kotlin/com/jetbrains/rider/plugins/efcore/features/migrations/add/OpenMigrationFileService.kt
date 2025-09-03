package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rd.util.reactive.filter

@Service(Service.Level.PROJECT)
class OpenMigrationFileService(val intellijProject: Project) {
    companion object {
        private const val MIGRATION_FILE_NAME_PATTERN = "^\\d{14}_%s.cs$"

        fun getInstance(intellijProject: Project) = intellijProject.service<OpenMigrationFileService>()
    }

    var fileListenerLifetimeDef: LifetimeDefinition? = null

    fun startOpeningFile(migrationsOutputFolderPath: String, migrationName: String) {
        val expectedFileNamePattern = MIGRATION_FILE_NAME_PATTERN.format(migrationName)
        fileListenerLifetimeDef = intellijProject.lifetime.createNested()

        FileEventsListenerService.getInstance(intellijProject)
            .fileCreated.filter { isMigrationFile(it, expectedFileNamePattern, migrationsOutputFolderPath) }
            .adviseOnce(fileListenerLifetimeDef!!.lifetime) {
                openFileInEditor(it)
                stopOpeningFile()
            }
    }

    fun stopOpeningFile() {
        fileListenerLifetimeDef?.terminate()
        fileListenerLifetimeDef = null
    }

    private fun isMigrationFile(file: VirtualFile, migrationNamePattern: String, migrationsFolderPath: String) =
        file.isFile && VfsUtil.pathEqualsTo(file.parent, migrationsFolderPath) &&
                Regex(migrationNamePattern).matches(file.name)


    private fun openFileInEditor(file: VirtualFile) =
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(intellijProject).openFile(file, true)
        }
}