package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bindNullable
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.dialog.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.*
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem
import javax.swing.DefaultComboBoxModel

class GenerateScriptDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?
) : BaseDialogWrapper(toolsVersion, "Generate SQL Script", intellijProject, selectedDotnetProjectName, true) {

    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Data binding
    private val dataCtx = GenerateScriptDataContext(intellijProject, commonCtx, beModel)

    //
    // Internal data
    private val fromMigrationsModel = DefaultComboBoxModel(arrayOf<MigrationItem>())
    private val toMigrationsModel = DefaultComboBoxModel(arrayOf<MigrationItem>())

    private val fromMigration = ObservableProperty<MigrationItem>(null)
    private val toMigration = ObservableProperty<MigrationItem>(null)

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

        dataCtx.availableFromMigrations.afterChange {
            fromMigrationsModel.removeAllElements()

            if (it != null) {
                fromMigrationsModel.addAll(it.map { MigrationItem(it) })
            }
        }

        dataCtx.availableToMigrations.afterChange {
            toMigrationsModel.removeAllElements()

            if (it != null) {
                toMigrationsModel.addAll(it.map { MigrationItem(it) })
            }
        }

        fromMigration.bindNullable(dataCtx.fromMigration,
            { fromMigrationsModel.firstOrNull { item -> item.data == it } },
            { it?.data })

        fromMigration.bindNullable(dataCtx.fromMigration,
            { toMigrationsModel.firstOrNull { item -> item.data == it } },
            { it?.data })
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()
        val fromMigration = dataCtx.fromMigration.notNullValue.trim()
        val toMigration = dataCtx.toMigration.value?.trim()
        val outputFile = dataCtx.outputFilePath.notNullValue
        val idempotent = dataCtx.idempotent.notNullValue
        val noTransactions = dataCtx.noTransactions.notNullValue

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
            iconComboBox(fromMigrationsModel, fromMigration)
                .validationOnApply(validator.fromMigrationValidation())
                .validationOnInput(validator.fromMigrationValidation())
                .comment("'0' means before the first migration")
                .horizontalAlign(HorizontalAlign.FILL)
                .monospaced()
                .focused()
        }

        row("To migration:") {
            iconComboBox(toMigrationsModel, toMigration)
                .horizontalAlign(HorizontalAlign.FILL)
                .monospaced()
                .focused()
        }
    }
}