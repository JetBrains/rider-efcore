package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsClient
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import java.io.File

class RemoveLastMigrationAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            RemoveLastMigrationDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val migrationsClient = intellijProject.getService<MigrationsClient>()
            val migrationsFolderService = RemoveLastMigrationFolderService()
            val commonOptions = getCommonOptions(dialog)
            val currentDbContextMigrationsList = dialog.availableMigrationsList
            val migration = currentDbContextMigrationsList.firstOrNull()

            executeCommandUnderProgress(intellijProject, "Removing migration...", "Last migration has been removed") {
                val res = migrationsClient.removeLast(commonOptions)

                migrationsFolderService.deleteMigrationsFolderIfEmpty(migration)

                res
            }
        }
    }
}