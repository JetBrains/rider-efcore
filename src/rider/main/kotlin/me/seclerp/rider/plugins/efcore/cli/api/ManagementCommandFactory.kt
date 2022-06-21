package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solutionDirectoryPath
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import java.nio.charset.Charset

@Service
class ManagementCommandFactory(intellijProject: Project) : BaseCommandFactory(intellijProject.solutionDirectoryPath.toString()) {
    fun installEfCoreTools(): CliCommand {
        val generalCommandLine =
            GeneralCommandLine("dotnet", "tool", "install", "--global", "dotnet-ef")
                .withCharset(Charset.forName("UTF-8"))

        val command = CliCommand(generalCommandLine)
        return command
    }
}