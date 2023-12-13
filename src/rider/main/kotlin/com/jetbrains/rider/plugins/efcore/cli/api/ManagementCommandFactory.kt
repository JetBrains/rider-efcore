package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo

@Service(Service.Level.PROJECT)
class ManagementCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<ManagementCommandFactory>()
    }

    fun installEfCoreTools(): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("install.dotnet.tool.presentable.name"),
            EfCoreUiBundle.message("ef.core.global.tools.have.been.successfully.installed"))

        return DotnetCliCommandBuilder(presentation, intellijProject, "tool", "install").apply {
            add("--ignore-failed-sources")
            addNamed("--add-source", "https://api.nuget.org/v3/index.json")
            add("--global")
            add("dotnet-ef")
        }.build()
    }
}