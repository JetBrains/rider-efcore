package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.observables.ui.dsl.bindSelected
import me.seclerp.observables.ui.dsl.bindText
import me.seclerp.observables.ui.dsl.iconComboBox
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem

class GenerateScriptDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectName: String?
) : CommonDialogWrapper<GenerateScriptDataContext>(
    GenerateScriptDataContext(intellijProject),
    toolsVersion,
    "Generate SQL Script",
    intellijProject,
    selectedProjectName,
    requireMigrationsInProject = true
) {
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Internal data
    private val fromMigrationsView = observableList<MigrationItem?>()
    private val toMigrationsView = observableList<MigrationItem?>()

    private val fromMigrationView = observable<MigrationItem?>(null)
    private val toMigrationView = observable<MigrationItem?>(null)

    //
    // Validation
    private val validator = GenerateScriptValidator()

    //
    // Constructor
    init {
        initUi()
    }

    override fun initBindings() {
        super.initBindings()

        fromMigrationsView.bind(dataCtx.availableFromMigrationNames) {
            it.map(mappings.migration.toItem)
        }

        toMigrationsView.bind(dataCtx.availableToMigrationNames) {
            it.map(mappings.migration.toItem)
        }

        fromMigrationView.bind(fromMigrationsView) { it.lastOrNull() }
        toMigrationView.bind(toMigrationsView) { it.firstOrNull() }

        fromMigrationView.bind(dataCtx.fromMigration,
            mappings.migration.toItem,
            mappings.migration.fromItem)

        toMigrationView.bind(dataCtx.toMigration,
            mappings.migration.toItem,
            mappings.migration.fromItem)
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val fromMigration = dataCtx.fromMigration.value!!.trim()
        val toMigration = dataCtx.toMigration.value?.trim()
        val outputFile = dataCtx.outputFilePath.value
        val idempotent = dataCtx.idempotent.value
        val noTransactions = dataCtx.noTransactions.value

        return migrationsCommandFactory.generateScript(
            efCoreVersion, commonOptions, fromMigration, toMigration, outputFile, idempotent, noTransactions)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        createMigrationRows()
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options") {
            row("Output file") {
                textField()
                    .bindText(dataCtx.outputFilePath)
                    .validationOnApply(validator.outputFileValidation())
                    .validationOnInput(validator.outputFileValidation())
            }
            row {
                checkBox("Make script idempotent")
                    .bindSelected(dataCtx.idempotent)
            }
            if (efCoreVersion.major >= 5) {
                row {
                    checkBox("No transactions")
                        .bindSelected(dataCtx.noTransactions)
                }
            }
        }
    }

    private fun Panel.createMigrationRows() {
        row("From migration:") {
            iconComboBox(fromMigrationView, fromMigrationsView)
                .validationOnApply(validator.fromMigrationValidation())
                .validationOnInput(validator.fromMigrationValidation())
                .comment("'0' means before the first migration")
                .horizontalAlign(HorizontalAlign.FILL)
                .focused()
        }

        row("To migration:") {
            iconComboBox(toMigrationView, toMigrationsView)
                .horizontalAlign(HorizontalAlign.FILL)
                .focused()
        }
    }

    companion object {
        private object mappings {
            object migration {
                val toItem: (String?) -> MigrationItem?
                    get() = {
                        if (it == null) null else MigrationItem(it)
                    }

                val fromItem: (MigrationItem?) -> String?
                    get() = { it?.data }
            }
        }
    }
}