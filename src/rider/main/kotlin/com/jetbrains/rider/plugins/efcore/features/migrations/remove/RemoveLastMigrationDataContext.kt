package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class RemoveLastMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true, true) {
    private val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()

        return migrationsCommandFactory.removeLast(commonOptions)
    }
}
