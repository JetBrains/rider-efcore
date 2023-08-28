package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class AddMigrationAction : BaseCommandAction<AddMigrationDataContext>(EfCoreUiBundle.message("new.migration.has.been.created")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = AddMigrationDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}