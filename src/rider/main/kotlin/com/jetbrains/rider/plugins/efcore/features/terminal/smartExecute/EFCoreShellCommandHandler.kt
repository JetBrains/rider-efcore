package com.jetbrains.rider.plugins.efcore.features.terminal.smartExecute

import com.intellij.execution.Executor
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
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
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.shared.QuickActionsGroup

class EFCoreShellCommandHandler : TerminalShellCommandHandler {

    companion object {
        // Define dotnet ef constant string and commands
        private const val DOTNET_EF = "dotnet ef"
        private val knownEfCommands = setOf(
            KnownEfCommands.Migrations.add,
            KnownEfCommands.Database.drop,
            KnownEfCommands.Database.update,
            KnownEfCommands.DbContext.scaffold,
            KnownEfCommands.Migrations.remove,
            KnownEfCommands.DbContext.script
        )
    }

    // Check if input command matches known commands
    override fun matches(project: Project, workingDirectory: String?, localSession: Boolean, command: String): Boolean {
        val parsedCommand = command.trim()
        return when {
            !parsedCommand.startsWith(DOTNET_EF) -> false
            parsedCommand == DOTNET_EF -> true
            else -> parsedCommand.removePrefix(DOTNET_EF).trim() in knownEfCommands
        }
    }

    // Execute input command
    override fun execute(project: Project, workingDirectory: String?, localSession: Boolean, command: String, executor: Executor): Boolean {
        if (command.trim() == DOTNET_EF) {
            val dataContext = SimpleDataContext.getProjectContext(project)
            val popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(
                    EfCoreUiBundle.message("popup.title.ef.core.quick.actions"),
                    QuickActionsGroup(),
                    dataContext,
                    JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING,
                    false
                )
            popup.showCenteredInCurrentWindow(project)
            return true
        }
        val dotnetProjectId = null
        return with(command.trim().removePrefix(DOTNET_EF).trim()) {
            when {
                startsWith(KnownEfCommands.Migrations.add) -> AddMigrationAction().launch(project, dotnetProjectId)
                startsWith(KnownEfCommands.Database.drop) -> DropDatabaseAction().launch(project, dotnetProjectId)
                startsWith(KnownEfCommands.Database.update) -> UpdateDatabaseAction().launch(project, dotnetProjectId)
                startsWith(KnownEfCommands.DbContext.scaffold) -> ScaffoldDbContextAction().launch(project, dotnetProjectId)
                startsWith(KnownEfCommands.Migrations.remove) -> RemoveLastMigrationAction().launch(project, dotnetProjectId)
                startsWith(KnownEfCommands.DbContext.script) -> GenerateScriptAction().launch(project, dotnetProjectId)
                else -> false
            }
        }
    }

    private fun BaseCommandAction.launch(project: Project, dotnetProjectId: UUID?): Boolean {
        val efCoreDefinition = project.solution.riderEfCoreModel.efToolsDefinition.valueOrNull ?: return false
        val toolsVersion = DotnetEfVersion.parse(efCoreDefinition.version) ?: return false
        return createDialog(project, toolsVersion, project.solution.riderEfCoreModel, dotnetProjectId).showAndGet()
    }
}