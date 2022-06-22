package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class UpdateDatabaseAction : BaseCommandAction("Updating database...", "Database has been updated") {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectName: String?
    ): BaseDialogWrapper =
        UpdateDatabaseDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectName)
}