package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.rd.Language
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo

class GenerateScriptDataContext(
    intellijProject: Project
): CommonDataContext(intellijProject, true) {
    val availableFromMigrations = observableList<MigrationInfo>()
    val availableToMigrations = observableList<MigrationInfo>()

    val fromMigration = observable<MigrationInfo?>(null)
    val toMigration = observable<MigrationInfo?>(null)
    val outputFilePath = observable("script.sql")
    val idempotent = observable(false)
    val noTransactions = observable(false)

    private val observableMigrations = ObservableMigrations(intellijProject, migrationsProject, dbContext)
    private val availableMigrations = observableList<MigrationInfo>()
        .apply {
            bind(observableMigrations) { migrations -> migrations
                .map { it }
                .sortedByDescending { it.migrationLongName }
            }
        }

    override fun initBindings() {
        super.initBindings()

        observableMigrations.initBinding()

        availableFromMigrations.bind(availableMigrations) {
            buildList {
                addAll(it)
                add(MigrationInfo("", "0", "0", "", it.lastOrNull()?.language ?: Language.Unknown))
            }
        }

        availableToMigrations.bind(availableMigrations) {
            buildList {
                addAll(it)
            }
        }
    }
}