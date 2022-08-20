package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class UpdateDatabaseDataContext(intellijProject: Project): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)

    var targetMigration = observable("")
    var useDefaultConnection = observable(true)
    var connection = observable("")

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()

        targetMigration.bind(availableMigrations) {
            if (it.isNotEmpty())
                it.first().migrationLongName
            else
                ""
        }
    }
}