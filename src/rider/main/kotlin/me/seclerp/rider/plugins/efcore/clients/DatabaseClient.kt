package me.seclerp.rider.plugins.efcore.clients

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.commands.CommonOptions
import me.seclerp.rider.plugins.efcore.commands.KnownEfCommands

@Service
class DatabaseClient : BaseEfCoreClient() {
    fun update(options: CommonOptions, targetMigration: String, connectionString: String? = null): CliCommandResult {
        val command = createCommand(KnownEfCommands.Database.update, options) {
            add(targetMigration)

            if (connectionString != null) {
                addNamed("--connection", "\"${connectionString}\"")
            }
        }

        return command.execute()
    }
}