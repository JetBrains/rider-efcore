package com.jetbrains.rider.plugins.efcore.features.database.update

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.jetbrains.observables.bind
import com.jetbrains.observables.observable
import com.jetbrains.observables.observableList
import com.jetbrains.observables.ui.dsl.bindSelected
import com.jetbrains.observables.ui.dsl.editableComboBox
import com.jetbrains.observables.ui.dsl.iconComboBox
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.ui.DbConnectionItemRenderer
import com.jetbrains.rider.plugins.efcore.ui.items.DbConnectionItem
import com.jetbrains.rider.plugins.efcore.ui.items.MigrationItem
import java.util.*

class UpdateDatabaseDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?
) : CommonDialogWrapper<UpdateDatabaseDataContext>(
    UpdateDatabaseDataContext(intellijProject, toolsVersion),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.Database.UpdateDatabaseAction.text"),
    intellijProject,
    selectedProjectId,
    true
) {

    val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

    //
    // Internal data
    private val targetMigrationsView = observableList<MigrationItem?>()
    private val targetMigrationView = observable<MigrationItem?>(null)
    private val availableDbConnectionsView = observableList<DbConnectionItem>()

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

        availableDbConnectionsView.bind(dataCtx.observableConnections) {
            it.map(mappings.dbConnection.toItem)
        }
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        row(EfCoreUiBundle.message("target.migration")) {
            iconComboBox(targetMigrationView, targetMigrationsView)
                .validationOnApply { dataCtx.targetMigrationValidation((it.selectedItem as? MigrationItem)?.data) }
                .validationOnInput { dataCtx.targetMigrationValidation((it.selectedItem as? MigrationItem)?.data) }
                .comment(EfCoreUiBundle.message("undo.all.applied.migrations.comment"))
                .align(AlignX.FILL)
                .focused()
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange(EfCoreUiBundle.message("section.additional.options")) {
            if (efCoreVersion.major >= 5) {
                var useDefaultConnectionCheckbox: JBCheckBox? = null
                row {
                    useDefaultConnectionCheckbox =
                        checkBox(EfCoreUiBundle.message("checkbox.use.default.connection.startup.project"))
                            .bindSelected(dataCtx.useDefaultConnection)
                            .component
                }
                row(EfCoreUiBundle.message("connection")) {
                    editableComboBox(dataCtx.connection, availableDbConnectionsView) { it.connectionString }
                        .applyToComponent { renderer = DbConnectionItemRenderer() }
                        .validationOnInput { dataCtx.connectionValidation((it.selectedItem as? DbConnectionItem)?.data?.connectionString) }
                        .validationOnApply { dataCtx.connectionValidation((it.selectedItem as? DbConnectionItem)?.data?.connectionString) }
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

            object dbConnection {
                val toItem: (DbConnectionInfo) -> DbConnectionItem
                    get() = {
                        DbConnectionItem(it)
                    }

                val fromItem: (DbConnectionItem) -> DbConnectionInfo
                    get() = { it.data }
            }
        }
    }
}