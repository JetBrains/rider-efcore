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

    fun add(options: CommonOptions, migrationName: String, outputDirectory: String? = null, namespace: String? = null): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("add.migration.presentable.name"),
            EfCoreUiBundle.message("new.migration.has.been.created"))

        return EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.add, options, presentation).apply {
            add(migrationName)
            addNamedNullable("--output-dir", outputDirectory)
            addNamedNullable("--namespace", namespace)
        }.build()
    }

    fun removeLast(options: CommonOptions): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("remove.last.migration.presentable.name"),
            EfCoreUiBundle.message("last.migration.has.been.removed"))

        return EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.remove, options, presentation).apply {
            add("--force")
        }.build()
    }

    fun generateScript(efCoreVersion: DotnetEfVersion, options: CommonOptions, fromMigration: String, toMigration: String?,
                       outputFile: String, idempotent: Boolean, noTransactions: Boolean): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("generate.sql.script.presentable.name"),
            EfCoreUiBundle.message("script.has.been.generated"))

        return EfCoreCommandBuilder(intellijProject, KnownEfCommands.Migrations.script, options, presentation).apply {
            add(fromMigration)
            addNullable(toMigration)
            addNamed("--output", outputFile)
            addIf("--idempotent", idempotent)
            if (efCoreVersion.major >= 5) {
                addIf("--no-transactions", noTransactions)
            }
        }.build()
    }
}