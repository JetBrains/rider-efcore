package me.seclerp.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class DropDatabaseAction : BaseCommandAction("Deleting database...", "Database has been deleted") {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectName: String?
    ): BaseDialogWrapper =
        DropDatabaseDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectName)
}