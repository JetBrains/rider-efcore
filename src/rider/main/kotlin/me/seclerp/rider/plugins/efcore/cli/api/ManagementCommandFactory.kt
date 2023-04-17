package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.execution.DotnetCommandBuilder

@Service(Service.Level.PROJECT)
class ManagementCommandFactory(private val intellijProject: Project) {
    fun installEfCoreTools(): GeneralCommandLine =
        DotnetCommandBuilder(intellijProject, "tool", "install").apply {
            add("--ignore-failed-sources")
            addNamed("--add-source", "https://api.nuget.org/v3/index.json")
            add("--global")
            add("dotnet-ef")
        }.build()
}