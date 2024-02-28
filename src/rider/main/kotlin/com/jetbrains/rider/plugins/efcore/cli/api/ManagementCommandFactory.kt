package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo

@Service(Service.Level.PROJECT)
class ManagementCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<ManagementCommandFactory>()
    }

    fun installGlobalTools(version: DotnetEfVersion? = null): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("install.global.dotnet.tool.presentable.name"),
            EfCoreUiBundle.message("ef.core.global.tools.have.been.successfully.installed"))

        return prepareToolInstallCommand(presentation).apply {
            add("--global")
            add("dotnet-ef")
        }.build()
    }

    fun installLocalTools(version: DotnetEfVersion? = null): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("install.local.dotnet.tool.presentable.name"),
            EfCoreUiBundle.message("ef.core.local.tools.have.been.successfully.installed"))

        return prepareToolInstallCommand(presentation).apply {
            add("dotnet-ef")
            addNamedNullable("--version", version.toString())
        }.build()
    }

    private fun prepareToolInstallCommand(presentation: CliCommandPresentationInfo) =
        DotnetCliCommandBuilder(presentation, intellijProject, "tool", "install").apply {
            add("--ignore-failed-sources")
            add("--create-manifest-if-needed")
            addNamed("--add-source", "https://api.nuget.org/v3/index.json")
        }
}