package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class GenerateScriptDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    val availableFromMigrations = observableList<String>()
    val availableToMigrations = observableList<String>()

    val fromMigration = observable<String?>(null)
    val toMigration = observable<String?>(null)
    val outputFilePath = observable("script.sql")
    val idempotent = observable(false)
    val noTransactions = observable(false)

    override fun initBindings() {
        super.initBindings()

        availableMigrations.initBinding()

        availableFromMigrations.bind(availableMigrations) {
            buildList {
                addAll(it.map { it.migrationLongName })
                add("0")
            }
        }

        availableToMigrations.bind(availableMigrations) {
            buildList {
                addAll(it.map { it.migrationLongName })
            }
        }
    }
}