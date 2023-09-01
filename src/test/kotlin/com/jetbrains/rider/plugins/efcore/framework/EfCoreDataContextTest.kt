package com.jetbrains.rider.plugins.efcore.framework

import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import kotlin.test.assertNotNull

@Suppress("HardCodedStringLiteral")
abstract class EfCoreDataContextTest : EfCoreTest() {
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

    fun <T : CommonDataContext> prepareContext(contextProvider: () -> T): T {
        return contextProvider().apply {
            initBindings()
            initData()
        }
    }

    fun <T : CommonDataContext> T.executeCommand(): CliCommandResult {
        return executeCommand(generateCommand())
    }
}