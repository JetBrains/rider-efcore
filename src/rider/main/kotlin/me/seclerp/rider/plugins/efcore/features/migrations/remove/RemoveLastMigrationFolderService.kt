package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import java.io.File

@Service
class RemoveLastMigrationFolderService {
    fun deleteMigrationsFolderIfEmpty(migration: MigrationInfo?) {
        val folder = migration?.migrationFolderAbsolutePath

        if (migration == null || folder == null) return

        val folderIsEmpty = folderIsEmpty(folder)

        if (folderIsEmpty) {
            File(folder).delete()
        }
    }

    private fun folderIsEmpty(folderPath: String): Boolean {
        val files = File(folderPath).listFiles()
        val isEmpty = files?.isEmpty() ?: false

        return isEmpty
    }
}