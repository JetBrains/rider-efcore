package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rider.plugins.efcore.features.shared.flushFileSystem
import com.jetbrains.rider.plugins.efcore.rd.MigrationIdentity
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Service(Service.Level.PROJECT)
class OpenMigrationFileService(private val intellijProject: Project) {
    suspend fun tryOpenMigrationFile(migrationFolderPath: String, migrationIdentity: MigrationIdentity): Boolean {
        intellijProject.flushFileSystem(migrationFolderPath)

        val beModel = intellijProject.solution.riderEfCoreModel
        val migration = withContext(Dispatchers.Main) {
            beModel.getMigration.startSuspending(migrationIdentity)
        }

        if (migration == null)
            return false

        val filePath = Path(migration.migrationFolderAbsolutePath, "${migration.migrationLongName}.cs")
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath.pathString)

        if (virtualFile == null)
            return false

        openFileInEditor(virtualFile)
        return true
    }

    private fun openFileInEditor(file: VirtualFile) =
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(intellijProject).openFile(file, true)
        }
}