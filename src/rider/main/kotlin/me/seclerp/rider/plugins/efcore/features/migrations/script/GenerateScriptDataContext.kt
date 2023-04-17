package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import me.seclerp.observables.*
import me.seclerp.rider.plugins.efcore.features.shared.ObservableMigrations
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.state.DialogsStateService
import org.jetbrains.annotations.NonNls

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

    override fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.loadState(commonDialogState)

        commonDialogState.get(KnownStateKeys.OUTPUT_FILE)?.apply {
            outputFilePath.value = this
        }

        commonDialogState.getBool(KnownStateKeys.IDEMPOTENT)?.apply {
            idempotent.value = this
        }

        commonDialogState.getBool(KnownStateKeys.NO_TRANSACTIONS)?.apply {
            noTransactions.value = this
        }
    }

    override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.saveState(commonDialogState)

        commonDialogState.set(KnownStateKeys.OUTPUT_FILE, outputFilePath.value)
        commonDialogState.set(KnownStateKeys.IDEMPOTENT, idempotent.value)
        commonDialogState.set(KnownStateKeys.NO_TRANSACTIONS, noTransactions.value)
    }

    object KnownStateKeys {
        @NonNls
        const val OUTPUT_FILE = "outputFilePath"
        @NonNls
        const val IDEMPOTENT = "idempotent"
        @NonNls
        const val NO_TRANSACTIONS = "noTransactions"
    }
}