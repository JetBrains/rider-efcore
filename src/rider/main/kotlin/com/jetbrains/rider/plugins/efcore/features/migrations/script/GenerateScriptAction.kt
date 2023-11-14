package com.jetbrains.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import java.util.*

class GenerateScriptAction : BaseCommandAction() {
    override fun createDialog(
        intellijProject: Project,
        toolsVersion: DotnetEfVersion,
        model: RiderEfCoreModel,
        currentDotnetProjectId: UUID?
    ) = GenerateScriptDialogWrapper(toolsVersion, intellijProject, currentDotnetProjectId)
}