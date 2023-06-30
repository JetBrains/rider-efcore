package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.execution.CommonOptions
import com.jetbrains.rider.plugins.efcore.cli.execution.KnownEfCommands
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.EfCommandBuilder
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
@NonNls
class DatabaseCommandFactory(private val intellijProject: Project) {
    fun update(efCoreVersion: DotnetEfVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.Database.update, options).apply {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", connectionString)
            }
        }.build()

    fun drop(options: CommonOptions): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.Database.drop, options).apply {
            add("--force")
        }.build()
}