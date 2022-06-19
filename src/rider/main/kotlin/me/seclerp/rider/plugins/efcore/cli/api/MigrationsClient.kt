package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.openapi.components.Service
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.cli.execution.KnownEfCommands

@Service
class MigrationsClient : BaseEfCoreClient() {
    fun add(options: CommonOptions, migrationName: String, outputDirectory: String? = null, namespace: String? = null): CliCommandResult {
        val command = createCommand(KnownEfCommands.Migrations.add, options) {
            add(migrationName)
            addNamedNullable("--output-dir", outputDirectory)
            addNamedNullable("--namespace", namespace)
        }

        return command.execute()
    }

    fun removeLast(options: CommonOptions): CliCommandResult {
        val command = createCommand(KnownEfCommands.Migrations.remove, options) {
            add("--force")
        }

        return command.execute()
    }
}