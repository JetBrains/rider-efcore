package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class ScaffoldDbContextAction : BaseCommandAction("DbContext has been scaffolded") {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectName: String?
    ) = ScaffoldDbContextDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectName)
}