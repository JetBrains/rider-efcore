package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CommonOptions
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.EfCoreCommandBuilder
import com.jetbrains.rider.plugins.efcore.cli.execution.KnownEfCommands
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
@NonNls
class DatabaseCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<DatabaseCommandFactory>()
    }

    fun update(efCoreVersion: DotnetEfVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): DotnetCommand =
        EfCoreCommandBuilder(intellijProject, KnownEfCommands.Database.update, options, EfCoreUiBundle.message("update.database.presentable.name")).apply {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", connectionString)
            }
        }.build()

    fun drop(options: CommonOptions): DotnetCommand =
        EfCoreCommandBuilder(intellijProject, KnownEfCommands.Database.drop, options, EfCoreUiBundle.message("drop.database.presentable.name")).apply {
            add("--force")
        }.build()
}