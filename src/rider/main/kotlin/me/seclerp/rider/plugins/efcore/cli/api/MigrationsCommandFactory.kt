package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solutionDirectoryPath
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.execution.KnownEfCommands

@Service
class MigrationsCommandFactory(intellijProject: Project) : BaseCommandFactory(intellijProject.solutionDirectoryPath.toString()) {
    fun add(options: CommonOptions, migrationName: String, outputDirectory: String? = null, namespace: String? = null): CliCommand =
        createCommand(KnownEfCommands.Migrations.add, options) {
            add(migrationName)
            addNamedNullable("--output-dir", outputDirectory)
            addNamedNullable("--namespace", namespace)
        }

    fun removeLast(options: CommonOptions): CliCommand =
        createCommand(KnownEfCommands.Migrations.remove, options) {
            add("--force")
        }

    fun generateScript(efCoreVersion: DotnetEfVersion, options: CommonOptions, fromMigration: String, toMigration: String?,
                       outputFile: String, idempotent: Boolean, noTransactions: Boolean): CliCommand =
        createCommand(KnownEfCommands.Migrations.script, options) {
            add(fromMigration)
            addNullable(toMigration)
            addNamed("--output", outputFile)
            addIf("--idempotent", idempotent)
            if (efCoreVersion.major >= 5) {
                addIf("--no-transactions", noTransactions)
            }
        }
}