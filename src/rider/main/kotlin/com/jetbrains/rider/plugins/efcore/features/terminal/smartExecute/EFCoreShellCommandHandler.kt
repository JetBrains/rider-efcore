package com.jetbrains.rider.plugins.efcore.features.terminal.smartExecute

import com.intellij.execution.Executor
import com.intellij.openapi.project.Project
import com.intellij.terminal.TerminalShellCommandHandler
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.KnownEfCommands
import com.jetbrains.rider.plugins.efcore.features.database.drop.DropDatabaseAction
import com.jetbrains.rider.plugins.efcore.features.database.update.UpdateDatabaseAction
import com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold.ScaffoldDbContextAction
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationAction
import com.jetbrains.rider.plugins.efcore.features.migrations.remove.RemoveLastMigrationAction
import com.jetbrains.rider.plugins.efcore.features.migrations.script.GenerateScriptAction
import com.jetbrains.rider.plugins.efcore.features.shared.BaseCommandAction
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import java.util.UUID
import com.intellij.openapi.diagnostic.logger

private val LOG = logger<DotnetEfShellCommandHandler>()
class DotnetEfShellCommandHandler : TerminalShellCommandHandler {
    override fun matches(project: Project, workingDirectory: String?, localSession: Boolean, command: String): Boolean {
        val match = command.trim().startsWith("dotnet ef ")

        LOG.info(if(match) "Command matched" else "Command did not match")
        return match
    }

    override fun execute(project: Project, workingDirectory: String?, localSession: Boolean, command: String, executor: Executor): Boolean {
        val parsedCommand = command.trim()
        val dotnetProjectId = null

        LOG.info("Executing command: $parsedCommand")

        return when {
            parsedCommand.startsWith(KnownEfCommands.Migrations.add) -> AddMigrationAction().launch(project, dotnetProjectId)
            parsedCommand.startsWith(KnownEfCommands.Database.drop) -> DropDatabaseAction().launch(project, dotnetProjectId)
            parsedCommand.startsWith(KnownEfCommands.Database.update) -> UpdateDatabaseAction().launch(project, dotnetProjectId)
            parsedCommand.startsWith(KnownEfCommands.DbContext.scaffold) -> ScaffoldDbContextAction().launch(project, dotnetProjectId)
            parsedCommand.startsWith(KnownEfCommands.Migrations.remove) -> RemoveLastMigrationAction().launch(project, dotnetProjectId)
            parsedCommand.startsWith(KnownEfCommands.DbContext.script) -> GenerateScriptAction().launch(project, dotnetProjectId)
            else -> false
        }
    }

    private fun BaseCommandAction.launch(project: Project, dotnetProjectId: UUID?): Boolean {
        val efCoreDefinition = project.solution.riderEfCoreModel.efToolsDefinition.valueOrNull
        val toolsVersion = efCoreDefinition?.let { DotnetEfVersion.parse(it.version) }!!

        LOG.info("Launching with tools version: $toolsVersion")

        return createDialog(project, toolsVersion, project.solution.riderEfCoreModel, dotnetProjectId).showAndGet()
    }
}