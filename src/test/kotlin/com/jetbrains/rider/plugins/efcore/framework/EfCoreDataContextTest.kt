package com.jetbrains.rider.plugins.efcore.framework

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.test.base.BaseTestWithSolution
import kotlin.test.assertNotNull

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

    fun <T : CommonDataContext> prepareContext(contextProvider: () -> T): T {
        return contextProvider().apply {
            initBindings()
            initData()
        }
    }
}