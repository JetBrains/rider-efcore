package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class AddMigrationDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val migrationName = observable("")
    val migrationsOutputFolder = observable("Migrations")

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()

        migrationName.bind(availableMigrations) {
            if (it.isEmpty() && migrationName.value.trim() == "")
                "Initial"
            else
                migrationName.value
        }
    }
}
