package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class RemoveLastMigrationAction : BaseCommandAction(EfCoreUiBundle.message("last.migration.has.been.removed")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = RemoveLastMigrationDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}