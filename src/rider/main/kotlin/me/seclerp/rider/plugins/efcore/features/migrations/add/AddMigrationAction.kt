package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class AddMigrationAction : BaseCommandAction(EfCoreUiBundle.message("new.migration.has.been.created")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = AddMigrationDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}