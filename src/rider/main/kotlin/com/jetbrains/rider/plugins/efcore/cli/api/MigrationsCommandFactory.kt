package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CommonOptions
import com.jetbrains.rider.plugins.efcore.cli.execution.EfCommandBuilder
import com.jetbrains.rider.plugins.efcore.cli.execution.KnownEfCommands

@Service(Service.Level.PROJECT)
class MigrationsCommandFactory(private val intellijProject: Project) {
    fun add(options: CommonOptions, migrationName: String, outputDirectory: String? = null, namespace: String? = null): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.Migrations.add, options).apply {
            add(migrationName)
            addNamedNullable("--output-dir", outputDirectory)
            addNamedNullable("--namespace", namespace)
        }.build()

    fun removeLast(options: CommonOptions): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.Migrations.remove, options).apply {
            add("--force")
        }.build()

    fun generateScript(efCoreVersion: DotnetEfVersion, options: CommonOptions, fromMigration: String, toMigration: String?,
                       outputFile: String, idempotent: Boolean, noTransactions: Boolean): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.Migrations.script, options).apply {
            add(fromMigration)
            addNullable(toMigration)
            addNamed("--output", outputFile)
            addIf("--idempotent", idempotent)
            if (efCoreVersion.major >= 5) {
                addIf("--no-transactions", noTransactions)
            }
        }.build()
}