package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.util.lifetime.waitTermination
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.cli.api.DotnetCliCommandBuilder
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.findProjects
import com.jetbrains.rider.projectView.workspace.isProject
import com.jetbrains.rider.test.base.BaseTestWithSolution
import java.util.*
import kotlin.test.assertTrue

abstract class EfCoreTest : BaseTestWithSolution() {
    suspend fun installEfLocalTools(version: DotnetEfVersion) {
        val presentation = CliCommandPresentationInfo("Manifest", "Manifest created")
        val initManifest = DotnetCliCommandBuilder(presentation, project, "new", "tool-manifest", "--force").build()
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

    suspend fun executeCommand(command: CliCommand): CliCommandResult {
        val patchedCommand = command.copy(
            commandLine = command.commandLine.withEnvironment("DOTNET_ROLL_FORWARD", "LatestMajor"))

        val executionLifetime = project.lifetime.createNested()
        val commandResult = TestCommandExecutor(project).execute(patchedCommand)
        executionLifetime.waitTermination()
        return commandResult ?: throw NotImplementedError()
    }
}