package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.*
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
@NonNls
class DatabaseCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<DatabaseCommandFactory>()
    }

    fun update(efCoreVersion: DotnetEfVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("update.database.presentable.name"),
            EfCoreUiBundle.message("database.has.been.updated"))

        return EfCoreCommandBuilder(intellijProject, KnownEfCommands.Database.update, options, presentation).apply {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", connectionString)
            }
        }.build()
    }

    fun drop(options: CommonOptions): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("drop.database.presentable.name"),
            EfCoreUiBundle.message("database.has.been.deleted"))

        return EfCoreCommandBuilder(intellijProject, KnownEfCommands.Database.drop, options, presentation).apply {
            add("--force")
        }.build()
    }
}