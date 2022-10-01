package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solutionDirectoryPath
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.execution.KnownEfCommands
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion

@Service
class DatabaseCommandFactory(intellijProject: Project) : BaseToolsCommandFactory(intellijProject.solutionDirectoryPath.toString()) {
    fun update(efCoreVersion: DotnetEfVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): GeneralCommandLine =
        createCommand(KnownEfCommands.Database.update, options) {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", connectionString)
            }
        }

    fun drop(options: CommonOptions): GeneralCommandLine {
        return createCommand(KnownEfCommands.Database.drop, options) {
            add("--force")
        }
    }
}