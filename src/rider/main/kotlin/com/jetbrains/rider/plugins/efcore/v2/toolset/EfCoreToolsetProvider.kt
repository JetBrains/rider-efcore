package com.jetbrains.rider.plugins.efcore.v2.toolset

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution

@Service(Service.Level.PROJECT)
internal class EfCoreToolsetProvider(private val project: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<EfCoreToolsetProvider>()
    }

    fun getToolset(): EfCoreToolset {
        val toolsDefinition = project.solution.riderEfCoreModel.cliToolsDefinition.valueOrNull
        val toolsVersion = toolsDefinition?.let { DotnetEfVersion.parse(it.version) }

        return
    }
}

data class EfCoreToolset(
    val toolsVersion: String,
)