package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import me.seclerp.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.dialog.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.*

class UpdateDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?
) : BaseDialogWrapper(toolsVersion, "Update Database", intellijProject, selectedDotnetProjectName, true) {

    val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

    //
    // Data binding
    private val dataCtx = UpdateDatabaseDataContext(intellijProject, commonCtx, beModel)

    //
    // Internal data
    private val currentDbContextMigrationsList = mutableListOf<String>()

    //
    // Validation
    private val validator = UpdateDatabaseValidator(currentDbContextMigrationsList)

    //
    // Constructor
    init {
        dataCtx.availableMigrations.afterChange {
            currentDbContextMigrationsList.clear()
            currentDbContextMigrationsList.addAll(it!!.map { it.migrationLongName })
            currentDbContextMigrationsList.add("0")
        }

        init()
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()
        val targetMigration = dataCtx.targetMigration.notNullValue.trim()
        val connection = if (dataCtx.useDefaultConnection.notNullValue) null else dataCtx.connection.notNullValue

        return databaseCommandFactory.update(efCoreVersion, commonOptions, targetMigration, connection)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        createTargetMigrationRow()
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options") {
            if (efCoreVersion.major >= 5) {
                var useDefaultConnectionCheckbox: JBCheckBox? = null
                row {
                    useDefaultConnectionCheckbox =
                        checkBox("Use default connection of startup project")
                            .bindSelected(dataCtx.useDefaultConnection)
                            .component
                }
                row("Connection:") {
                    textField()
                        .bindText(dataCtx.connection)
                        .validationOnInput(validator.connectionValidation())
                        .validationOnApply(validator.connectionValidation())
                        .enabledIf(useDefaultConnectionCheckbox!!.selected.not())
                }
            }
        }
    }

    private fun Panel.createTargetMigrationRow() {
        row("Target migration:") {
            textFieldWithCompletion(dataCtx.targetMigration, currentDbContextMigrationsList, intellijProject, completionItemsIcon)
                .horizontalAlign(HorizontalAlign.FILL)
                .comment("Use <code>0</code> as a target migration to undo all applied migrations")
                .focused()
                .validationOnInput(validator.targetMigrationValidation())
                .validationOnApply(validator.targetMigrationValidation())
                .monospaced()
        }
    }

    companion object {
        val completionItemsIcon = DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS)
    }
}