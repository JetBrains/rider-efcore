package com.jetbrains.rider.plugins.efcore.features.migrations.script

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.jetbrains.observables.*
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableMigrations
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.rd.MigrationInfo
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import org.jetbrains.annotations.NonNls

class GenerateScriptDataContext(
    intellijProject: Project,
    private val efCoreVersion: DotnetEfVersion
): CommonDataContext(intellijProject, true, true) {
    private val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

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

    val outputFileValidation: (String?) -> ValidationInfo? = {
        if (it?.trim().isNullOrEmpty())
            error(EfCoreUiBundle.message("dialog.message.script.output.file.could.not.be.empty"))
        else
            null
    }

    val fromMigrationValidation: (String?) -> ValidationInfo? = {
        if (it == null)
            error(EfCoreUiBundle.message("dialog.message.from.migration.should.be.specified"))
        else
            null
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

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val fromMigration = fromMigration.value!!.trim()
        val toMigration = toMigration.value?.trim()
        val outputFile = outputFilePath.value
        val idempotent = idempotent.value
        val noTransactions = noTransactions.value

        return migrationsCommandFactory.generateScript(
            efCoreVersion, commonOptions, fromMigration, toMigration, outputFile, idempotent, noTransactions)
    }

    override fun validate() = buildList {
        addAll(super.validate())
        outputFileValidation(outputFilePath.value)?.let { add(it) }
        fromMigrationValidation(fromMigration.value)?.let { add(it) }
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