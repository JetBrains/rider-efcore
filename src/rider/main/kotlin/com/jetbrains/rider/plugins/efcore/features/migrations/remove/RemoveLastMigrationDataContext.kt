package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class RemoveLastMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()
    }
}
