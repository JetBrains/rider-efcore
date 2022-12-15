package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.observables.ui.dsl.bindSelected
import me.seclerp.observables.ui.dsl.bindText
import me.seclerp.observables.ui.dsl.iconComboBox
import me.seclerp.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.*
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem
import java.util.*

class UpdateDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?
) : CommonDialogWrapper<UpdateDatabaseDataContext>(
    UpdateDatabaseDataContext(intellijProject),
    toolsVersion,
    "Update Database",
    intellijProject,
    selectedProjectId,
    true
) {

    val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

    //
    // Internal data
    private val targetMigrationsView = observableList<MigrationItem?>()

    private val targetMigrationView = observable<MigrationItem?>(null)

    //
    // Validation
    private val validator = UpdateDatabaseValidator(targetMigrationsView)

    //
    // Constructor
    init {
        initUi()
    }

    override fun initBindings() {
        super.initBindings()

        targetMigrationsView.bind(dataCtx.availableMigrationNames) {
            it.map(mappings.migration.toItem)
        }

        targetMigrationView.bind(targetMigrationsView){ it.firstOrNull() }

        targetMigrationView.bind(dataCtx.targetMigration,
            mappings.migration.toItem,
            mappings.migration.fromItem)
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val targetMigration = dataCtx.targetMigration.value!!.trim()
        val connection = if (dataCtx.useDefaultConnection.value) null else dataCtx.connection.value

        return databaseCommandFactory.update(efCoreVersion, commonOptions, targetMigration, connection)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        row("Target migration:") {
            iconComboBox(targetMigrationView, targetMigrationsView)
                .validationOnApply(validator.targetMigrationValidation())
                .validationOnInput(validator.targetMigrationValidation())
                .comment("Use <code>0</code> as a target migration to undo all applied migrations")
                .horizontalAlign(HorizontalAlign.FILL)
                .focused()
        }
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