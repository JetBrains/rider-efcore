package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommandBuilder

@Service(Service.Level.PROJECT)
class ManagementCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<ManagementCommandFactory>()
    }

    fun installEfCoreTools(): DotnetCommand =
        DotnetCommandBuilder(EfCoreUiBundle.message("install.dotnet.tool.presentable.name"), intellijProject, "tool", "install").apply {
            add("--ignore-failed-sources")
            addNamed("--add-source", "https://api.nuget.org/v3/index.json")
            add("--global")
            add("dotnet-ef")
        }.build()
}