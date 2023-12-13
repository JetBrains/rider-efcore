package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo
import com.jetbrains.rider.plugins.efcore.cli.execution.KnownEfCommands
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions
import com.jetbrains.rider.run.withRawParameters
import java.nio.file.Paths

class EfCoreCliCommandBuilder(
    intellijProject: Project,
    baseCommand: String,
    private val commonOptions: DialogCommonOptions,
    presentation: CliCommandPresentationInfo
) : DotnetCliCommandBuilder(
    presentation,
    intellijProject,
    KnownEfCommands.ef, baseCommand
) {
    init {
        addNamed("--project", makeRelativeProjectPath(commonOptions.migrationsProject.fullPath))
        addNamed("--startup-project", makeRelativeProjectPath(commonOptions.startupProject.fullPath))
        addNamedNullable("--context", commonOptions.dbContext?.fullName)
        addNamed("--configuration", commonOptions.buildConfiguration)
        addNamedNullable("--framework", commonOptions.targetFramework)
        addIf("--no-build", commonOptions.noBuild)
        addIf("--verbose", commonOptions.enableDiagnosticLogging)
    }

    override fun build(): CliCommand {
        val command = super.build()
        var commandLine = command.commandLine
        if (commonOptions.additionalArguments.isNotEmpty()) {
            add("--")
            commandLine = commandLine.withRawParameters(commonOptions.additionalArguments)
        }

        return CliCommand(command.dotnetPath, commandLine, command.presentationInfo)
    }

    private fun makeRelativeProjectPath(projectDirectory: String): String {
        val base = Paths.get(solutionDirectory)
        val absolute = Paths.get(projectDirectory)
        val relative = base.relativize(absolute)

        return relative.toString()
    }
}