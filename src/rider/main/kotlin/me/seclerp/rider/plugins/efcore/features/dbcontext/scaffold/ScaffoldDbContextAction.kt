package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class ScaffoldDbContextAction : BaseCommandAction("DbContext has been scaffolded") {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = ScaffoldDbContextDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}