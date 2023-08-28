package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommandBuilder

@Service(Service.Level.PROJECT)
class ManagementCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<ManagementCommandFactory>()
    }

    fun installGlobalTools(version: DotnetEfVersion? = null): GeneralCommandLine =
        prepareToolInstallCommand().apply {
            add("--global")
            add("dotnet-ef")
        }.build()

    fun installLocalTools(version: DotnetEfVersion? = null): GeneralCommandLine =
        prepareToolInstallCommand().apply {
            add("dotnet-ef")
            addNamedNullable("--version", version.toString())
        }.build()

    private fun prepareToolInstallCommand() =
        DotnetCommandBuilder(intellijProject, "tool", "install").apply {
            add("--ignore-failed-sources")
            add("--create-manifest-if-needed")
            addNamed("--add-source", "https://api.nuget.org/v3/index.json")
        }
}