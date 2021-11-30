package me.seclerp.rider.plugins.efcore.commands

class CliCommandBuilder(baseCommand: String, commonOptions: CommonOptions) {
    private val commandStringBuilder: StringBuilder = StringBuilder(KnownEfCommands.dotnetEf)

    init {
        commandStringBuilder.append(" ", baseCommand)

        addNamed("--project", commonOptions.migrationsProject)
        addNamed("--startup-project", commonOptions.startupProject)
        addNamed("--configuration", commonOptions.buildConfiguration)
        addNamed("--framework", commonOptions.targetFramework)
        addIf("--no-build", commonOptions.noBuild)
    }

    fun add(value: String): CliCommandBuilder {
        commandStringBuilder.append(" ", value)

        return this
    }

    fun addIf(key: String, condition: Boolean): CliCommandBuilder {
        if (condition)
            commandStringBuilder.append(" ", key)

        return this
    }

    fun addNamed(name: String, value: String): CliCommandBuilder {
        commandStringBuilder.append(" ", name, " ", value)

        return this
    }

    fun addNamedNullable(name: String, value: String?): CliCommandBuilder {
        if (value != null)
            commandStringBuilder.append(" ", name, " ", value)

        return this
    }

    fun build(): CliCommand {
        val fullCommand = commandStringBuilder.toString()

        return CliCommand(fullCommand)
    }
}