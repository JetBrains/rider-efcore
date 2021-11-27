package me.seclerp.rider.plugins.efcore.clients

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.commands.CommonOptions
import me.seclerp.rider.plugins.efcore.commands.KnownEfCommands

@Service
class MigrationsClient(project: Project) : BaseEfCoreClient() {
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