package com.jetbrains.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.UUID

class DropDatabaseAction : BaseCommandAction(EfCoreUiBundle.message("database.has.been.deleted")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = DropDatabaseDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}