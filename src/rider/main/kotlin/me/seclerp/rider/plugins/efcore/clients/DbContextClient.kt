package me.seclerp.rider.plugins.efcore.clients

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.commands.CommonOptions
import me.seclerp.rider.plugins.efcore.commands.KnownEfCommands
import me.seclerp.rider.plugins.efcore.models.EfCoreVersion

@Service
class DbContextClient : BaseEfCoreClient() {
    fun scaffold(efCoreVersion: EfCoreVersion, options: CommonOptions, connection: String, provider: String,
                 outputFolder: String, useAttributes: Boolean, useDatabaseNames: Boolean, generateOnConfiguring: Boolean,
                 usePluralizer: Boolean, dbContextName: String, dbContextFolder: String, scaffoldAllTables: Boolean,
                 tablesList: List<String>, scaffoldAllSchemas: Boolean, schemasList: List<String>): CliCommandResult {
        val command = createCommand(KnownEfCommands.DbContext.scaffold, options) {
            add(connection)
            add(provider)

            addIf("--data-annotations", useAttributes)
            addNamed("--context", dbContextName)
            addNamed("--context-dir", dbContextFolder)
            //addNamed("--force", force)
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
        }

        return command.execute()
    }
}