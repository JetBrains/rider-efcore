package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.util.idea.getService
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsClient
import me.seclerp.rider.plugins.efcore.cli.execution.executeCommandUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreAction

class AddMigrationAction : EfCoreAction() {
    override fun ready(actionEvent: AnActionEvent, efCoreVersion: DotnetEfVersion) {
        val intellijProject = actionEvent.project!!
        val dialog = buildDialogInstance(actionEvent) {
            AddMigrationDialogWrapper(model, intellijProject, currentDotnetProjectName)
        }

        if (dialog.showAndGet()) {
            val migrationsClient = intellijProject.getService<MigrationsClient>()
            val commonOptions = getCommonOptionsV2(dialog)

            executeCommandUnderProgress(intellijProject, "Creating migration...", "New migration has been created") {
                migrationsClient.add(commonOptions, dialog.model.migrationName.trim())
            }
        }
    }
}