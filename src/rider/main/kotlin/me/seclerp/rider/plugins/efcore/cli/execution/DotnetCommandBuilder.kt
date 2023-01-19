package me.seclerp.rider.plugins.efcore.cli.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.dotNetActiveRuntimeModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.solutionDirectoryPath
import com.jetbrains.rider.run.FormatPreservingCommandLine
import com.jetbrains.rider.run.withRawParameters
import java.io.File
import java.nio.charset.Charset

open class DotnetCommandBuilder(private val intellijProject: Project, vararg baseCommands: String) {
    private val activeRuntime by lazy { intellijProject.solution.dotNetActiveRuntimeModel.activeRuntime.valueOrNull }
    protected val solutionDirectory = intellijProject.solutionDirectoryPath.toString()

    private var generalCommandLine: GeneralCommandLine =
        FormatPreservingCommandLine()
            .withExePath(getDotnetExePath())
            .withRawParameters(baseCommands.joinToString(" "))
            .withCharset(Charset.forName("UTF-8"))
            .withWorkDirectory(solutionDirectory)
            .withEnvironment("DOTNET_ROOT", getDotnetRootPath())
            .withEnvironment("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")
            .withEnvironment("DOTNET_NOLOGO", "true")

    fun add(value: String) {
        generalCommandLine = generalCommandLine.withParameters(value)
    }

    fun addNullable(value: String?) {
        if (value != null)
            generalCommandLine = generalCommandLine.withParameters(value)
    }

    fun addIf(key: String, condition: Boolean) {
        if (condition)
            generalCommandLine = generalCommandLine.withParameters(key)
    }

    fun addNamed(name: String, value: String) {
        generalCommandLine = generalCommandLine.withParameters(name, value)
    }

    fun addNamedNullable(name: String, value: String?) {
        if (value != null)
            generalCommandLine = generalCommandLine.withParameters(name, value)
    }

    open fun build() = generalCommandLine

    private fun getDotnetExePath() =
        activeRuntime?.dotNetCliExePath
            ?: throw Exception(".NET / .NET Core is not configured, unable to run commands.")

    private fun getDotnetRootPath() = File(getDotnetExePath()).parent
}