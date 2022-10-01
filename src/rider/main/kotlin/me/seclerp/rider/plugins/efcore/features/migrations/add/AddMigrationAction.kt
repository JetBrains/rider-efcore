package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class AddMigrationAction : BaseCommandAction("New migration has been created") {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectName: String?
    ) = AddMigrationDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectName)
}