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

    private companion object {
        private const val MIGRATION_FILE_NAME_PATTERN = "^\\d{14}_%s.cs$"
    }

    var fileListenerLifetimeDef: LifetimeDefinition? = null
    var expectedFileNamePattern: String? = null
    var migrationsFolderPath: String? = null

    fun startOpeningFile(migrationsOutputFolderPath: String, migrationName: String) {
        expectedFileNamePattern = MIGRATION_FILE_NAME_PATTERN.format(migrationName)
        migrationsFolderPath = migrationsOutputFolderPath
        fileListenerLifetimeDef = intellijProject.lifetime.createNested()

        intellijProject.service<AddMigrationVfsListenerService>()
            .fileCreated.filter { isMigrationFile(it) }
            .adviseOnce(fileListenerLifetimeDef!!.lifetime) {
                openFileInEditor(it)
                stopOpeningFile()
            }
    }

    fun stopOpeningFile() {
        fileListenerLifetimeDef?.terminate()
        fileListenerLifetimeDef = null
        expectedFileNamePattern = null
        migrationsFolderPath = null
    }

    private fun isMigrationFile(file: VirtualFile) =
        file.isFile && VfsUtil.pathEqualsTo(file.parent, migrationsFolderPath!!) &&
                Regex(expectedFileNamePattern!!).matches(file.name)


    private fun openFileInEditor(file: VirtualFile) =
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(intellijProject).openFile(file, true)
        }
}