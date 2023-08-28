package com.jetbrains.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class UpdateDatabaseAction : BaseCommandAction<UpdateDatabaseDataContext>(EfCoreUiBundle.message("database.has.been.updated")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = UpdateDatabaseDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}