package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.ui.*
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import javax.swing.DefaultComboBoxModel

class GenerateScriptDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?
) : BaseDialogWrapper3(toolsVersion, "Generate SQL Script", intellijProject, selectedDotnetProjectName, true) {

    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Data binding
    private val model = GenerateScriptModel(null, null, "script.sql", false, false)

    //
    // Internal data
    private var fromMigrationsModel: DefaultComboBoxModel<MigrationItem> = DefaultComboBoxModel()
    private var toMigrationsModel: DefaultComboBoxModel<MigrationItem> = DefaultComboBoxModel()

    //
    // Validation
    private val validator = GenerateScriptValidator()

    //
    // Constructor
    init {
        addMigrationsProjectChangedListener(::onMigrationsProjectChanged)
        addDbContextChangedListener(::onDbContextChanged)

        init()
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()
        val fromMigration = model.fromMigration!!.data.trim()
        val toMigration = model.toMigration?.data?.trim()
        val outputFile = model.outputFilePath
        val idempotent = model.idempotent
        val noTransactions = model.noTransactions

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
                    .bindText(model::outputFilePath)
                    .validationOnApply(validator.outputFileValidation())
                    .validationOnInput(validator.outputFileValidation())
            }
            row {
                checkBox("Make script idempotent")
                    .bindSelected(model::idempotent)
            }
            if (efCoreVersion.major >= 5) {
                row {
                    checkBox("No transactions")
                        .bindSelected(model::noTransactions)
                }
            }
        }
    }

    private fun Panel.createMigrationRows() {
        row("From migration:") {
            iconComboBox(fromMigrationsModel, model::fromMigration)
                .validationOnApply(validator.fromMigrationValidation())
                .validationOnInput(validator.fromMigrationValidation())
                .comment("'0' means before the first migration")
                .horizontalAlign(HorizontalAlign.FILL)
                .focused()
        }

        row("To migration:") {
            iconComboBox(toMigrationsModel, model::toMigration)
                .horizontalAlign(HorizontalAlign.FILL)
                .focused()
        }
    }

    //
    // Event listeners
    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        refreshCurrentDbContextMigrations(commonOptions.dbContext)
    }

    private fun onDbContextChanged(dbContext: DbContextItem?) {
        refreshCurrentDbContextMigrations(dbContext)
    }

    private fun refreshCurrentDbContextMigrations(dbContext: DbContextItem?) {
        fromMigrationsModel.removeAllElements()
        toMigrationsModel.removeAllElements()

        if (dbContext == null) {
            return
        }

        val migrationProjectName = commonOptions.migrationsProject!!.displayName
        val dbContextFullName = commonOptions.dbContext!!.data
        val migrationsIdentity = MigrationsIdentity(migrationProjectName, dbContextFullName)

        val availableDbContextMigrations = loadMigrationsByContextName(migrationsIdentity)
            .map { MigrationItem(it.migrationLongName) }

        fromMigrationsModel.addAll(0, availableDbContextMigrations)
        fromMigrationsModel.addElement(MigrationItem("0"))
        toMigrationsModel.addAll(0, availableDbContextMigrations)

        fromMigrationsModel.selectedItem = null
        if (availableDbContextMigrations.size > 0) {
            fromMigrationsModel.selectedItem = fromMigrationsModel.getElementAt(fromMigrationsModel.size - 1)
        }
        toMigrationsModel.selectedItem = toMigrationsModel.getElementAt(0)
    }

    private fun loadMigrationsByContextName(migrationsIdentity: MigrationsIdentity): List<MigrationInfo> {
        if (migrationsIdentity.dbContextClassFullName.isEmpty()) {
            return listOf()
        }

        return beModel.getAvailableMigrations
            .runUnderProgress(
                migrationsIdentity, intellijProject, "Loading migrations...",
                isCancelable = true,
                throwFault = true
            )?.sortedByDescending { it.migrationLongName } ?: listOf()
    }
}