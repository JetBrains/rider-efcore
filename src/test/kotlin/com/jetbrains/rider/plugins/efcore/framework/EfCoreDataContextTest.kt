package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.util.lifetime.waitTermination
import com.jetbrains.rider.plugins.efcore.cli.api.ManagementCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResultProcessor
import com.jetbrains.rider.plugins.efcore.cli.execution.DotnetCommandBuilder
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.test.base.BaseTestWithSolution
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("HardCodedStringLiteral")
abstract class EfCoreDataContextTest : BaseTestWithSolution() {
    fun <T : CommonDataContext> T.withEfProjects(startupProjectName: String, migrationsProjectName: String, action: T.() -> Unit) {
        val (previousStartup, previousMigrations) = startupProject.value to migrationsProject.value

        val foundStartupProject = assertNotNull(availableStartupProjects.firstOrNull { it.name == startupProjectName })
        val foundMigrationsProject = assertNotNull(availableMigrationsProjects.firstOrNull { it.name == migrationsProjectName })

        startupProject.value = foundStartupProject
        migrationsProject.value = foundMigrationsProject

        action()

        startupProject.value = previousStartup
        migrationsProject.value = previousMigrations
    }

    fun <T : CommonDataContext> T.assertValid() {
        assert(validate().isEmpty()) { "Validation should succeed" }
    }

    fun installEfLocalTools(version: DotnetEfVersion) {
        val initManifest = DotnetCommandBuilder(project, "new", "tool-manifest", "--force").build()
        assertTrue("Init manifest failed") { executeCommand(initManifest).succeeded }

        val installTools = ManagementCommandFactory.getInstance(project).installLocalTools(version)
        assertTrue("dotnet-ef installation failed") { executeCommand(installTools).succeeded }
    }

    fun <T : CommonDataContext> prepareContext(contextProvider: () -> T): T {
        return contextProvider().apply {
            initBindings()
            initData()
        }
    }

    fun <T : CommonDataContext> T.executeCommand(): CliCommandResult {
        return executeCommand(generateCommand())
    }

    private fun executeCommand(command: GeneralCommandLine): CliCommandResult {
        val patchedCommand = command
            .withEnvironment("DOTNET_ROLL_FORWARD", "LatestMajor")
        val executionLifetime = project.lifetime.createNested()
        var commandResult: CliCommandResult? = null
        TestCommandExecutor(project).execute(patchedCommand, object : CliCommandResultProcessor() {
            override fun doProcess(result: CliCommandResult, retryAction: () -> Unit) {
                commandResult = result
                executionLifetime.terminate()
            }
        })
        executionLifetime.waitTermination()
        return commandResult ?: throw NotImplementedError()
    }
}