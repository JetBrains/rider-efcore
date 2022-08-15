package me.seclerp.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solutionDirectoryPath
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import java.nio.charset.Charset

@Service
class ManagementCommandFactory(private val intellijProject: Project) {
    fun installEfCoreTools(): CliCommand =
        createManagementCommand(
            "dotnet", "tool", "install",
            "--ignore-failed-sources",
            "--add-source", "https://api.nuget.org/v3/index.json",
            "--global",
            "dotnet-ef")

    private fun createManagementCommand(vararg command: String): CliCommand {
        val generalCommandLine =
            GeneralCommandLine(*command)
                .withWorkDirectory(intellijProject.solutionDirectoryPath.toString())
                .withCharset(Charset.forName("UTF-8"))

        return CliCommand(generalCommandLine)
    }
}