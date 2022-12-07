package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.jetbrains.rider.run.FormatPreservingCommandLine
import com.jetbrains.rider.run.withRawParameters
import java.nio.charset.Charset
import java.nio.file.Paths

class CliCommandBuilder(private val solutionDirectory: String, baseCommand: String, private val commonOptions: CommonOptions) {
    private var generalCommandLine: GeneralCommandLine =
        FormatPreservingCommandLine()
            .withExePath(KnownEfCommands.dotnet)
            .withParameters(KnownEfCommands.ef)
            .withParameters(baseCommand.split(" "))
            .withCharset(Charset.forName("UTF-8"))
            .withWorkDirectory(solutionDirectory)
            .withEnvironment("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")
            .withEnvironment("DOTNET_NOLOGO", "true")

    init {
        addNamed("--project", makeRelativeProjectPath(commonOptions.migrationsProject))
        addNamed("--startup-project", makeRelativeProjectPath(commonOptions.startupProject))
        addNamedNullable("--context", commonOptions.dbContext)
        addNamed("--configuration", commonOptions.buildConfiguration)
        addNamedNullable("--framework", commonOptions.targetFramework)
        addIf("--no-build", commonOptions.noBuild)
    }

    fun add(value: String): CliCommandBuilder {
        generalCommandLine = generalCommandLine.withParameters(value)

        return this
    }

    fun addNullable(value: String?): CliCommandBuilder {
        if (value != null)
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

    fun build(): GeneralCommandLine {
        if (commonOptions.additionalArguments.isNotEmpty()) {
            add("--")
            generalCommandLine = generalCommandLine.withRawParameters(commonOptions.additionalArguments)
        }

        return generalCommandLine
    }

    private fun makeRelativeProjectPath(projectDirectory: String): String {
        val base = Paths.get(solutionDirectory)
        val absolute = Paths.get(projectDirectory)
        val relative = base.relativize(absolute)

        return relative.toString()
    }
}