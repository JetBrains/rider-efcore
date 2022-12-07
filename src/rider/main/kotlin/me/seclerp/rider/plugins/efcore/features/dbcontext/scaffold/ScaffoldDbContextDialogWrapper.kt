package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.ui.table.JBTable
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.rider.plugins.efcore.cli.api.DbContextCommandFactory
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem
import me.seclerp.rider.plugins.efcore.ui.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.observables.ui.dsl.bindSelected
import me.seclerp.observables.ui.dsl.bindText
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import java.io.File
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class ScaffoldDbContextDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectName: String?,
) : CommonDialogWrapper<ScaffoldDbContextDataContext>(
    ScaffoldDbContextDataContext(intellijProject),
    toolsVersion,
    "Scaffold DbContext",
    intellijProject,
    selectedProjectName,
    requireMigrationsInProject = false
) {
    val dbContextCommandFactory = intellijProject.service<DbContextCommandFactory>()

    //
    // Internal data
    private lateinit var mainTab: DialogPanel
    private lateinit var dbContextTab: DialogPanel
    private lateinit var tablesTab: DialogPanel
    private lateinit var schemaTab: DialogPanel

    private val tablesModel = SimpleListTableModel(dataCtx.tablesList)
    private val schemasModel = SimpleListTableModel(dataCtx.schemasList)

    private val migrationProjectFolder = observable("")

    //
    // Validation
    private val validator = ScaffoldDbContextValidator()

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
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()

        return dbContextCommandFactory.scaffold(
            efCoreVersion, commonOptions,
            dataCtx.connection.value,
            dataCtx.provider.value,
            dataCtx.outputFolder.value,
            dataCtx.useAttributes.value,
            dataCtx.useDatabaseNames.value,
            dataCtx.generateOnConfiguring.value,
            dataCtx.usePluralizer.value,
            dataCtx.dbContextName.value,
            dataCtx.dbContextFolder.value,
            dataCtx.scaffoldAllTables.value,
            dataCtx.tablesList.map { it.data },
            dataCtx.scaffoldAllSchemas.value,
            dataCtx.schemasList.map { it.data })
    }

    //
    // UI
    override fun createCenterPanel(): JComponent {
        val tabbedPane = JBTabbedPane()

        mainTab = createMainTab()
        dbContextTab = createDbContextTab()
        tablesTab = createTablesTab()
        schemaTab = createSchemasTab()

        tabbedPane.addTab("Main", mainTab)
        tabbedPane.addTab("DbContext", dbContextTab)
        tabbedPane.addTab("Tables", tablesTab)
        tabbedPane.addTab("Schemas", schemaTab)

        return panel {
            row {
                cell(tabbedPane)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .verticalAlign(VerticalAlign.FILL)
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
        row("Connection:") {
            textField()
                .bindText(dataCtx.connection)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.connectionValidation())
                .validationOnApply(validator.connectionValidation())
        }

        row("Provider:") {
            textField()
                .bindText(dataCtx.provider)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.providerValidation())
                .validationOnApply(validator.providerValidation())
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options") {
            row("Output folder:") {
                textFieldForRelativeFolder(
                    migrationProjectFolder.getter,
                    intellijProject,
                    "Select Output Folder")
                    .bindText(dataCtx.outputFolder)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput(validator.outputFolderValidation())
                    .validationOnApply(validator.outputFolderValidation())
                    .applyToComponent { dataCtx.migrationsProject.afterChange { this.isEnabled = it != null }  }
            }

            row {
                checkBox("Use attributes to generate the model")
                    .bindSelected(dataCtx.useAttributes)
            }

            row {
                checkBox("Use database names")
                    .bindSelected(dataCtx.useDatabaseNames)
            }

            if (efCoreVersion.major >= 5) {
                row {
                    checkBox("Generate OnConfiguring method")
                        .bindSelected(dataCtx.generateOnConfiguring)
                }

                row {
                    checkBox("Use the pluralizer")
                        .bindSelected(dataCtx.usePluralizer)
                }
            }
        }
    }

    private fun createDbContextTab(): DialogPanel = panel {
        row("Generated DbContext name:") {
            textField()
                .bindText(dataCtx.dbContextName)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.dbContextNameValidation())
                .validationOnInput(validator.dbContextNameValidation())
        }

        row("Generated DbContext folder:") {
            textFieldForRelativeFolder(
                migrationProjectFolder.getter,
                intellijProject,
                "Select Generated DbContext Folder")
                .bindText(dataCtx.dbContextFolder)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.dbContextFolderValidation())
                .validationOnInput(validator.dbContextFolderValidation())
                .applyToComponent { dataCtx.migrationsProject.afterChange { this.isEnabled = it != null }  }
        }
    }

    private fun createTablesTab(): DialogPanel =
        createToggleableTablePanel(tablesModel, "Scaffold all tables", dataCtx.scaffoldAllTables)

    private fun createSchemasTab(): DialogPanel =
        createToggleableTablePanel(schemasModel, "Scaffold all schemas", dataCtx.scaffoldAllSchemas)

    private fun createToggleableTablePanel(
        tableModel: SimpleListTableModel,
        checkboxText: String,
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
                cell(tablePanel)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .verticalAlign(VerticalAlign.FILL)
                    .enabledIf(enabledCheckbox!!.selected.not())
            }.resizableRow()
        }
    }
}