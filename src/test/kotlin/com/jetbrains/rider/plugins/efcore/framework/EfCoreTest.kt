package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.util.lifetime.waitTermination
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResultProcessor
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommandBuilder
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.findProjects
import com.jetbrains.rider.projectView.workspace.isProject
import com.jetbrains.rider.test.base.BaseTestWithSolution
import java.util.*
import kotlin.test.assertTrue

abstract class EfCoreTest : BaseTestWithSolution() {
    fun installEfLocalTools(version: DotnetEfVersion) {
        val initManifest = DotnetCommandBuilder(project, "new", "tool-manifest", "--force").build()
        assertTrue("Init manifest failed") { executeCommand(initManifest).succeeded }

        val installTools = ManagementCommandFactory.getInstance(project).installLocalTools(version)
        assertTrue("dotnet-ef installation failed") { executeCommand(installTools).succeeded }
    }

    fun findProjectById(id: UUID): ProjectModelEntity? {
        return WorkspaceModel.getInstance(project)
            .findProjects()
            .filter { it.isProject() }
            .map { it to it.descriptor as RdProjectDescriptor }
            .firstOrNull { it.second.originalGuid == id }
            ?.first
    }

    fun executeCommand(command: GeneralCommandLine): CliCommandResult {
        val patchedCommand = command
            .withEnvironment("DOTNET_ROLL_FORWARD", "LatestMajor")
        val executionLifetime = project.lifetime.createNested()
        var commandResult: CliCommandResult? = null
        TestCommandExecutor(project).execute(patchedCommand, object : CliCommandResultProcessor() {
            override fun doProcess(result: CliCommandResult, retryAction: () -> Unit) {
                commandResult = result
                executionLifetime.terminate()
            }
        })
        executionLifetime.waitTermination()
        return commandResult ?: throw NotImplementedError()
    }
}