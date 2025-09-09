package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.rd.MigrationInfo
import java.io.File

@Service(Service.Level.PROJECT)
class RemoveLastMigrationFolderService {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<RemoveLastMigrationFolderService>()
    }

    fun deleteMigrationsFolderIfEmpty(migration: MigrationInfo?) {
        val folder = migration?.migrationFolderAbsolutePath ?: return
        if (folderIsEmpty(folder)) {
            File(folder).delete()
        }
    }

    private fun folderIsEmpty(folderPath: String) =
        File(folderPath).listFiles()?.isEmpty() ?: false
}