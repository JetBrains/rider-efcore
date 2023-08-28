package com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.ui.table.JBTable
import com.jetbrains.observables.*
import com.jetbrains.rider.plugins.efcore.cli.api.DbContextCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.observables.ui.dsl.bindSelected
import com.jetbrains.observables.ui.dsl.bindText
import com.jetbrains.observables.ui.dsl.editableComboBox
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.rd.DbProviderInfo
import com.jetbrains.rider.plugins.efcore.ui.DbConnectionItemRenderer
import com.jetbrains.rider.plugins.efcore.ui.DbProviderItemRenderer
import com.jetbrains.rider.plugins.efcore.ui.items.*
import com.jetbrains.rider.plugins.efcore.ui.textFieldForRelativeFolder
import java.io.File
import java.util.*
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class ScaffoldDbContextDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<ScaffoldDbContextDataContext>(
    ScaffoldDbContextDataContext(intellijProject, toolsVersion),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.DbContext.ScaffoldDbContextAction.text"),
    intellijProject,
    selectedProjectId
) {
    //
    // Internal data
    private lateinit var mainTab: DialogPanel
    private lateinit var dbContextTab: DialogPanel
    private lateinit var tablesTab: DialogPanel
    private lateinit var schemaTab: DialogPanel

    private val tablesModel = SimpleListTableModel(dataCtx.tablesList)
    private val schemasModel = SimpleListTableModel(dataCtx.schemasList)

    private val migrationProjectFolder = observable("")
    private val availableDbConnectionsView = observableList<DbConnectionItem>()
    private val availableDbProvidersView = observableList<DbProviderItem>()

    //
    // Constructor
    init {
        initUi()
    }

    override fun initBindings() {
        super.initBindings()

        migrationProjectFolder.bind(dataCtx.migrationsProject) {
            if (it != null)
                File(it.fullPath).parentFile.path
            else
                ""
        }

        availableDbConnectionsView.bind(dataCtx.observableConnections) {
            it.map(mappings.dbConnection.toItem)
        }

        availableDbProvidersView.bind(dataCtx.observableDbProviders) {
            it.map(mappings.dbProvider.toItem)
        }
    }

    //
    // UI
    override fun createCenterPanel(): JComponent {
        val tabbedPane = JBTabbedPane()

        mainTab = createMainTab()
        dbContextTab = createDbContextTab()
        tablesTab = createTablesTab()
        schemaTab = createSchemasTab()

        tabbedPane.addTab(EfCoreUiBundle.message("tab.main"), mainTab)
        tabbedPane.addTab(EfCoreUiBundle.message("tab.dbcontext"), dbContextTab)
        tabbedPane.addTab(EfCoreUiBundle.message("tab.tables"), tablesTab)
        tabbedPane.addTab(EfCoreUiBundle.message("tab.schemas"), schemaTab)

        return panel {
            row {
                cell(tabbedPane)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            registerIntegratedPanel(mainTab)
            registerIntegratedPanel(dbContextTab)
            registerIntegratedPanel(tablesTab)
            registerIntegratedPanel(schemaTab)
            panel = this
        }
    }

    private fun createMainTab(): DialogPanel = createMainUI()

    override fun Panel.createPrimaryOptions() {
        row(EfCoreUiBundle.message("connection")) {
            editableComboBox(dataCtx.connection, availableDbConnectionsView) { it.connectionString }
                .applyToComponent { renderer = DbConnectionItemRenderer() }
                .align(AlignX.FILL)
                .validationOnInput { if (it.isEnabled) dataCtx.connectionValidation((it.selectedItem as? DbConnectionItem)?.data?.connectionString) else null }
                .validationOnApply { if (it.isEnabled) dataCtx.connectionValidation((it.selectedItem as? DbConnectionItem)?.data?.connectionString) else null }
        }

        row(EfCoreUiBundle.message("provider")) {
            editableComboBox(dataCtx.provider, availableDbProvidersView) { it.id }
                .applyToComponent { renderer = DbProviderItemRenderer() }
                .align(AlignX.FILL)
                .validationOnInput { if (it.isEnabled) dataCtx.providerValidation((it.selectedItem as? DbProviderItem)?.data) else null }
                .validationOnApply { if (it.isEnabled) dataCtx.providerValidation((it.selectedItem as? DbProviderItem)?.data) else null }
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange(EfCoreUiBundle.message("section.additional.options")) {
            row(EfCoreUiBundle.message("output.folder")) {
                textFieldForRelativeFolder(
                    migrationProjectFolder.getter,
                    intellijProject,
                    EfCoreUiBundle.message("select.output.folder"))
                    .bindText(dataCtx.outputFolder)
                    .align(AlignX.FILL)
                    .validationOnInput { dataCtx.outputFolderValidation(it.text) }
                    .validationOnApply { dataCtx.outputFolderValidation(it.text) }
                    .applyToComponent { dataCtx.migrationsProject.afterChange { this.isEnabled = it != null }  }
            }

            row {
                checkBox(EfCoreUiBundle.message("checkbox.use.attributes.to.generate.model"))
                    .bindSelected(dataCtx.useAttributes)
            }

            row {
                checkBox(EfCoreUiBundle.message("checkbox.use.database.names"))
                    .bindSelected(dataCtx.useDatabaseNames)
            }

            if (efCoreVersion.major >= 5) {
                row {
                    checkBox(EfCoreUiBundle.message("checkbox.generate.onconfiguring.method"))
                        .bindSelected(dataCtx.generateOnConfiguring)
                }

                row {
                    checkBox(EfCoreUiBundle.message("checkbox.use.pluralizer"))
                        .bindSelected(dataCtx.usePluralizer)
                }
            }
        }
    }

    private fun createDbContextTab(): DialogPanel = panel {
        row(EfCoreUiBundle.message("generated.dbcontext.name")) {
            textField()
                .bindText(dataCtx.dbContextName)
                .align(AlignX.FILL)
                .validationOnInput { dataCtx.dbContextNameValidation(it.text) }
                .validationOnApply { dataCtx.dbContextNameValidation(it.text) }
        }

        row(EfCoreUiBundle.message("generated.dbcontext.folder")) {
            textFieldForRelativeFolder(
                migrationProjectFolder.getter,
                intellijProject,
                EfCoreUiBundle.message("select.generated.dbcontext.folder"))
                .bindText(dataCtx.dbContextFolder)
                .align(AlignX.FILL)
                .validationOnInput { dataCtx.dbContextFolderValidation(it.text) }
                .validationOnApply { dataCtx.dbContextFolderValidation(it.text) }
                .applyToComponent { dataCtx.migrationsProject.afterChange { this.isEnabled = it != null }  }
        }
    }

    private fun createTablesTab(): DialogPanel =
        createToggleableTablePanel(tablesModel, EfCoreUiBundle.message("checkbox.scaffold.all.tables"), dataCtx.tablesList, dataCtx.scaffoldAllTables)

    private fun createSchemasTab(): DialogPanel =
        createToggleableTablePanel(schemasModel, EfCoreUiBundle.message("checkbox.scaffold.all.schemas"), dataCtx.schemasList, dataCtx.scaffoldAllSchemas)

    private fun createToggleableTablePanel(
        tableModel: SimpleListTableModel,
        checkboxText: String,
        items: ObservableCollection<SimpleItem>,
        checkboxProperty: ObservableProperty<Boolean>
    ): DialogPanel {
        val table = JBTable(tableModel)
            .apply {
                tableHeader.isVisible = false
            }

        val tableDecorator = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                tableModel.addRow(SimpleItem(""))
            }
            .setRemoveAction {
                TableUtil.removeSelectedItems(table)
            }

        val tablePanel = tableDecorator.createPanel()

        return panel {
            var enabledCheckbox: JBCheckBox? = null
            row {
                enabledCheckbox =
                    checkBox(checkboxText)
                        .bindSelected(checkboxProperty)
                        .component
            }

            enabledCheckbox!!.selected.addListener {
                tablePanel.isVisible = !it
            }

            tablePanel.isVisible = !enabledCheckbox!!.isSelected

            row {
                val scaffoldAll = enabledCheckbox!!.selected
                cell(tablePanel)
                    .align(Align.FILL)
                    .enabledIf(scaffoldAll.not())
                    .validationOnApply(dataCtx.tableSchemaValidation(items, scaffoldAll))
            }.resizableRow()
        }
    }

    companion object {
        private object mappings {
            object dbConnection {
                val toItem: (DbConnectionInfo) -> DbConnectionItem
                    get() = {
                        DbConnectionItem(it)
                    }

                val fromItem: (DbConnectionItem) -> DbConnectionInfo
                    get() = { it.data }
            }

            object dbProvider {
                val toItem: (DbProviderInfo) -> DbProviderItem
                    get() = {
                        DbProviderItem(it)
                    }

                val fromItem: (DbProviderItem) -> DbProviderInfo
                    get() = { it.data }
            }
        }
    }
}
