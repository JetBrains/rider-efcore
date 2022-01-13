package me.seclerp.rider.plugins.efcore.commands

import com.intellij.execution.configurations.GeneralCommandLine
import java.nio.charset.Charset

class CliCommandBuilder(baseCommand: String, commonOptions: CommonOptions) {
    private var generalCommandLine: GeneralCommandLine =
        GeneralCommandLine(KnownEfCommands.dotnet)
            .withParameters(KnownEfCommands.ef)
            .withParameters(baseCommand.split(" "))
            .withCharset(Charset.forName("UTF-8"))

    init {
        addNamed("--project", commonOptions.migrationsProject)
        addNamed("--startup-project", commonOptions.startupProject)
        addNamed("--context", commonOptions.dbContext)
        addNamed("--configuration", commonOptions.buildConfiguration)
        addNamed("--framework", commonOptions.targetFramework)
        addIf("--no-build", commonOptions.noBuild)
    }

    fun add(value: String): CliCommandBuilder {
        generalCommandLine = generalCommandLine.withParameters(value)

        return this
    }

    fun addIf(key: String, condition: Boolean): CliCommandBuilder {
        if (condition)
            generalCommandLine = generalCommandLine.withParameters(key)

        return this
    }

    fun addNamed(name: String, value: String): CliCommandBuilder {
        generalCommandLine = generalCommandLine.withParameters(name, value)

        return this
    }

    fun addNamedNullable(name: String, value: String?): CliCommandBuilder {
        if (value != null)
            generalCommandLine = generalCommandLine.withParameters(name, value)

        return this
    }

    fun build(): CliCommand {
        return CliCommand(generalCommandLine)
    }
}