package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.features.database.drop.DropDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.database.update.UpdateDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold.ScaffoldDbContextCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.remove.RemoveLastMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.script.GenerateScriptCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand

@Service(Service.Level.PROJECT)
class EfCoreCliCommandFactory(private val project: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<EfCoreCliCommandFactory>()
    }

    private val migrationsFactory by lazy { MigrationsCommandFactory.getInstance(project) }
    private val databaseFactory by lazy { DatabaseCommandFactory.getInstance(project) }
    private val dbContextFactory by lazy { DbContextCommandFactory.getInstance(project) }

    fun create(command: DialogCommand, efCoreVersion: DotnetEfVersion): CliCommand {
        return when (command) {
            is AddMigrationCommand -> migrationsFactory.add(command.common, command.migrationName, command.outputFolder)
            is RemoveLastMigrationCommand -> migrationsFactory.removeLast(command.common)
            is GenerateScriptCommand -> migrationsFactory.generateScript(efCoreVersion, command.common,
                command.fromMigration, command.toMigration, command.outputFilePath, command.idempotent, command.noTransactions)

            is DropDatabaseCommand -> databaseFactory.drop(command.common)
            is UpdateDatabaseCommand -> databaseFactory.update(efCoreVersion, command.common, command.targetMigration,
                command.connection)

            is ScaffoldDbContextCommand -> dbContextFactory.scaffold(efCoreVersion, command.common,
                command.connection,
                command.provider,
                command.outputFolder,
                command.useAttributes,
                command.useDatabaseNames,
                command.generateOnConfiguring,
                command.usePluralizer,
                command.dbContextName,
                command.dbContextFolder,
                command.scaffoldAllTables,
                command.tablesList,
                command.scaffoldAllSchemas,
                command.schemasList,
            )
            else -> throw IllegalArgumentException("Unsupported command type: ${command::class.simpleName}")
        }
    }
}