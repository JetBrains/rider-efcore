package me.seclerp.rider.plugins.efcore.clients

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.commands.CommonOptions
import me.seclerp.rider.plugins.efcore.commands.KnownEfCommands
import me.seclerp.rider.plugins.efcore.models.EfCoreVersion

@Service
class DatabaseClient : BaseEfCoreClient() {
    fun update(efCoreVersion: EfCoreVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): CliCommandResult {
        val command = createCommand(KnownEfCommands.Database.update, options) {
            add(targetMigration)

            if (efCoreVersion.major >= 5 && connectionString != null) {
                addNamed("--connection", "\"${connectionString}\"")
            }
        }

        return command.execute()
    }

    fun drop(options: CommonOptions): CliCommandResult {
        val command = createCommand(KnownEfCommands.Database.drop, options) {
            add("--force")
        }

        return command.execute()
    }
}