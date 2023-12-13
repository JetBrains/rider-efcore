package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.*
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
@NonNls
class DatabaseCommandFactory(private val intellijProject: Project) {
    companion object {
        fun getInstance(project: Project) = project.service<DatabaseCommandFactory>()
    }

    fun update(efCoreVersion: DotnetEfVersion, options: DialogCommonOptions, targetMigration: String, connectionString: String? = null): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("update.database.presentable.name"),
            EfCoreUiBundle.message("database.has.been.updated"))

        return EfCoreCliCommandBuilder(intellijProject, KnownEfCommands.Database.update, options, presentation).apply {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", connectionString)
            }
        }.build()
    }

    fun drop(options: DialogCommonOptions): CliCommand {
        val presentation = CliCommandPresentationInfo(
            EfCoreUiBundle.message("drop.database.presentable.name"),
            EfCoreUiBundle.message("database.has.been.deleted"))

        return EfCoreCliCommandBuilder(intellijProject, KnownEfCommands.Database.drop, options, presentation).apply {
            add("--force")
        }.build()
    }
}