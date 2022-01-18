package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.execution.KnownEfCommands
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion

@Service
class DatabaseClient : me.seclerp.rider.plugins.efcore.cli.api.BaseEfCoreClient() {
    fun update(efCoreVersion: DotnetEfVersion, options: CommonOptions, targetMigration: String, connectionString: String? = null): CliCommandResult {
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