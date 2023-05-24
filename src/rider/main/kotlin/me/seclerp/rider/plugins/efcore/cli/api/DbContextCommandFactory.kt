package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.execution.KnownEfCommands
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.EfCommandBuilder

@Service(Service.Level.PROJECT)
class DbContextCommandFactory(private val intellijProject: Project) {
    fun scaffold(efCoreVersion: DotnetEfVersion, options: CommonOptions, connection: String, provider: String,
                 outputFolder: String, useAttributes: Boolean, useDatabaseNames: Boolean, generateOnConfiguring: Boolean,
                 usePluralizer: Boolean, dbContextName: String, dbContextFolder: String, scaffoldAllTables: Boolean,
                 tablesList: List<String>, scaffoldAllSchemas: Boolean, schemasList: List<String>): GeneralCommandLine =
        EfCommandBuilder(intellijProject, KnownEfCommands.DbContext.scaffold, options).apply {
            add(connection)
            add(provider)

            addIf("--data-annotations", useAttributes)
            addNamed("--context", dbContextName)
            addNamed("--context-dir", dbContextFolder)
            add("--force")
            addNamed("--output-dir", outputFolder)

            if (!scaffoldAllSchemas) {
                schemasList.forEach {
                    addNamed("--schema", it)
                }
            }

            if (!scaffoldAllTables) {
                tablesList.forEach {
                    addNamed("--table", it)
                }
            }

            addIf("--use-database-names", useDatabaseNames)

            if (efCoreVersion.major >= 5) {
                addIf("--no-onconfiguring", !generateOnConfiguring)
                addIf("--no-pluralize", !usePluralizer)
            }
        }.build()
}