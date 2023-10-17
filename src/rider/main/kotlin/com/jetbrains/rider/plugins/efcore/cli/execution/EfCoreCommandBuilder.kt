package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.openapi.project.Project
import com.jetbrains.rider.run.withRawParameters
import java.nio.file.Paths

class EfCoreCommandBuilder(
    intellijProject: Project,
    baseCommand: String,
    private val commonOptions: CommonOptions,
    presentableName: String
) : DotnetCommandBuilder(
    presentableName,
    intellijProject,
    KnownEfCommands.ef, baseCommand
) {
    init {
        addNamed("--project", makeRelativeProjectPath(commonOptions.migrationsProject))
        addNamed("--startup-project", makeRelativeProjectPath(commonOptions.startupProject))
        addNamedNullable("--context", commonOptions.dbContext)
        addNamed("--configuration", commonOptions.buildConfiguration)
        addNamedNullable("--framework", commonOptions.targetFramework)
        addIf("--no-build", commonOptions.noBuild)
        addIf("--verbose", commonOptions.enableDiagnosticLogging)
    }

    override fun build(): DotnetCommand {
        val command = super.build()
        var commandLine = command.commandLine
        if (commonOptions.additionalArguments.isNotEmpty()) {
            add("--")
            commandLine = commandLine.withRawParameters(commonOptions.additionalArguments)
        }

        return DotnetCommand(command.dotnetPath, commandLine, command.presentableName)
    }

    private fun makeRelativeProjectPath(projectDirectory: String): String {
        val base = Paths.get(solutionDirectory)
        val absolute = Paths.get(projectDirectory)
        val relative = base.relativize(absolute)

        return relative.toString()
    }
}