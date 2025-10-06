package com.jetbrains.rider.plugins.efcore.cli.api

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.jetbrains.rider.ijent.extensions.toNioPathOrNull
import com.jetbrains.rider.model.dotNetActiveRuntimeModel
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.solutionDirectoryPath
import com.jetbrains.rider.shared.run.FormatPreservingPtyCommandLine
import com.jetbrains.rider.shared.run.withRawParameters
import org.jetbrains.annotations.NonNls
import java.nio.charset.Charset
import kotlin.io.path.pathString

open class DotnetCliCommandBuilder(
    private val presentation: CliCommandPresentationInfo,
    private val intellijProject: Project,
    vararg baseCommands: @NonNls String
) {
    private val activeRuntime by lazy { intellijProject.solution.dotNetActiveRuntimeModel.activeRuntime.valueOrNull }
    protected val solutionDirectory = intellijProject.solutionDirectoryPath.toString()

    @NonNls
    private var generalCommandLine: GeneralCommandLine =
        FormatPreservingPtyCommandLine()
            .withExePath(getDotnetExePath().pathString)
            .withRawParameters(baseCommands.joinToString(" "))
            .withCharset(Charset.forName("UTF-8"))
            .withWorkDirectory(solutionDirectory)
            .withEnvironment("DOTNET_ROOT", getDotnetRootPath().pathString)
            .withEnvironment("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")
            .withEnvironment("DOTNET_NOLOGO", "true")

    @NonNls
    fun add(value: String) {
        generalCommandLine = generalCommandLine.withParameters(value)
    }

    @NonNls
    fun addNullable(value: String?) {
        if (value != null)
            generalCommandLine = generalCommandLine.withParameters(value)
    }

    @NonNls
    fun addIf(key: String, condition: Boolean) {
        if (condition)
            generalCommandLine = generalCommandLine.withParameters(key)
    }

    @NonNls
    fun addNamed(name: String, value: String) {
        generalCommandLine = generalCommandLine.withParameters(name, value)
    }

    @NonNls
    fun addNamedNullable(name: String, value: String?) {
        if (value != null)
            generalCommandLine = generalCommandLine.withParameters(name, value)
    }

    open fun build() = CliCommand(generalCommandLine.exePath, generalCommandLine, presentation)

    private fun getDotnetExePath() =
        activeRuntime?.dotNetCliExePath?.toNioPathOrNull()
            ?: throw Exception(".NET / .NET Core is not configured, unable to run commands.")

    private fun getDotnetRootPath() = getDotnetExePath().parent
}