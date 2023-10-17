package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.*

@Service(Service.Level.PROJECT)
class MigrationsCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<MigrationsCommandFactory>()
    }

    fun add(options: CommonOptions, migrationName: String, outputDirectory: String? = null, namespace: String? = null): DotnetCommand =
        EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.add, options, EfCoreUiBundle.message("add.migration.presentable.name")).apply {
            add(migrationName)
            addNamedNullable("--output-dir", outputDirectory)
            addNamedNullable("--namespace", namespace)
        }.build()

    fun removeLast(options: CommonOptions): DotnetCommand =
        EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.remove, options, EfCoreUiBundle.message("remove.last.migration.presentable.name")).apply {
            add("--force")
        }.build()

    fun generateScript(efCoreVersion: DotnetEfVersion, options: CommonOptions, fromMigration: String, toMigration: String?,
                       outputFile: String, idempotent: Boolean, noTransactions: Boolean): DotnetCommand =
        EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.script, options, EfCoreUiBundle.message("generate.sql.script.presentable.name")).apply {
            add(fromMigration)
            addNullable(toMigration)
            addNamed("--output", outputFile)
            addIf("--idempotent", idempotent)
            if (efCoreVersion.major >= 5) {
                addIf("--no-transactions", noTransactions)
            }
        }.build()
}