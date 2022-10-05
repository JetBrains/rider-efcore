package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class GenerateScriptDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableFromMigrationNames = observableList<String>()
    val availableToMigrationNames = observableList<String>()

    val fromMigration = observable<String?>(null)
    val toMigration = observable<String?>(null)
    val outputFilePath = observable("script.sql")
    val idempotent = observable(false)
    val noTransactions = observable(false)

    private val observableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    private val availableMigrationNames = observableList<String>()
        .apply {
            bind(observableMigrations) { migrations -> migrations
                .map { it.migrationLongName }
                .sortedByDescending { it }
            }
        }

    override fun initBindings() {
        super.initBindings()

        observableMigrations.initBinding()

        availableFromMigrationNames.bind(availableMigrationNames) {
            buildList {
                addAll(it)
                add("0")
            }
        }

        availableToMigrationNames.bind(availableMigrationNames) {
            buildList {
                addAll(it)
            }
        }
    }
}