package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.jetbrains.rider.run.withRawParameters
import java.nio.file.Paths

class EfCommandBuilder(
    intellijProject: Project,
    baseCommand: String,
    private val commonOptions: CommonOptions
) : DotnetCommandBuilder(
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
    }

    override fun build(): GeneralCommandLine {
        var command = super.build()
        if (commonOptions.additionalArguments.isNotEmpty()) {
            add("--")
            command = command.withRawParameters(commonOptions.additionalArguments)
        }

        return command
    }

    private fun makeRelativeProjectPath(projectDirectory: String): String {
        val base = Paths.get(solutionDirectory)
        val absolute = Paths.get(projectDirectory)
        val relative = base.relativize(absolute)

        return relative.toString()
    }
}