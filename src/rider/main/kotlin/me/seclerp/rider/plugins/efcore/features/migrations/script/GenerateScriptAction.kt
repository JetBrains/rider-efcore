package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.BaseCommandAction
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class GenerateScriptAction : BaseCommandAction(EfCoreUiBundle.message("script.has.been.generated")) {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = GenerateScriptDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}