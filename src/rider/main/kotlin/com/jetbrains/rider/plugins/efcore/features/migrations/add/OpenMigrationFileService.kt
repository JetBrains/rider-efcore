package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rider.plugins.efcore.rd.AddMigrationInfo
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class OpenMigrationFileService(private val intellijProject: Project) {
    suspend fun openMigrationFile(migrationIdentity: AddMigrationInfo) {
        val beModel = intellijProject.solution.riderEfCoreModel

        withContext(Dispatchers.Main) {
            beModel.migrationFileCreated.adviseOnce(intellijProject.lifetime) {
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(it)

                if (virtualFile != null)
                    openFileInEditor(virtualFile)
            }

            beModel.addMigrationExecuted.fire(migrationIdentity)
        }
    }

    private fun openFileInEditor(file: VirtualFile) =
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(intellijProject).openFile(file, true)
        }
}