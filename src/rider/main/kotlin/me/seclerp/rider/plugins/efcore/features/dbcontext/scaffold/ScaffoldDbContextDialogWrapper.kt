package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
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
import me.seclerp.rider.plugins.efcore.cli.api.DbContextCommandFactory
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem
import me.seclerp.rider.plugins.efcore.ui.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import java.io.File
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

class ScaffoldDbContextDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?,
) : BaseDialogWrapper(toolsVersion, "Scaffold DbContext", intellijProject, selectedDotnetProjectName,
    requireMigrationsInProject = false, requireDbContext = false
) {
    val dbContextCommandFactory = intellijProject.service<DbContextCommandFactory>()

    //
    // Data binding
    val model = ScaffoldDbContextModel(
        connection = "",
        provider = "",
        outputFolder = "Entities",
        useAttributes = false,
        useDatabaseNames = false,
        generateOnConfiguring = true,
        usePluralizer = true,
        dbContextName = "MyDbContext",
        dbContextFolder = "Context",
        tablesList = mutableListOf(),
        schemasList = mutableListOf(),
        scaffoldAllTables = true,
        scaffoldAllSchemas = true,
    )

    //
    // Internal data
    private lateinit var mainTab: DialogPanel
    private lateinit var dbContextTab: DialogPanel
    private lateinit var tablesTab: DialogPanel
    private lateinit var schemaTab: DialogPanel

    private val tablesModel = SimpleListTableModel(model.tablesList)
    private val schemasModel = SimpleListTableModel(model.schemasList)

    //
    // Validation
    private val validator = ScaffoldDbContextValidator()

    //
    // Constructor
    init {
        init()
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()

        return dbContextCommandFactory.scaffold(
            efCoreVersion, commonOptions,
            model.connection,
            model.provider,
            model.outputFolder,
            model.useAttributes,
            model.useDatabaseNames,
            model.generateOnConfiguring,
            model.usePluralizer,
            model.dbContextName,
            model.dbContextFolder,
            model.scaffoldAllTables,
            model.tablesList.map { it.data },
            model.scaffoldAllSchemas,
            model.schemasList.map { it.data })
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
            validateCallbacks = buildList {
                addAll(mainTab.validateCallbacks)
                addAll(dbContextTab.validateCallbacks)
                addAll(tablesTab.validateCallbacks)
                addAll(schemaTab.validateCallbacks)
            }
        }
    }

    override fun doOKAction() {
        if (okAction.isEnabled) {
            mainTab.apply()
            dbContextTab.apply()
            tablesTab.apply()
            schemaTab.apply()
        }

        super.doOKAction()
    }

    private fun createMainTab(): DialogPanel =
        createMainUI()
            .apply(::configureValidation)

    override fun Panel.createPrimaryOptions() {
        row("Connection:") {
            textField().bindText(model::connection)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.connectionValidation())
                .validationOnApply(validator.connectionValidation())
        }

        row("Provider:") {
            textField().bindText(model::provider)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.providerValidation())
                .validationOnApply(validator.providerValidation())
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options") {
            row("Output folder:") {
                textFieldForRelativeFolder(
                    ::currentMigrationsProjectFolderGetter,
                    intellijProject,
                    "Select Output Folder")
                    .bindText(model::outputFolder)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput(validator.outputFolderValidation())
                    .validationOnApply(validator.outputFolderValidation())
                    .applyToComponent { isEnabled = commonOptions.migrationsProject != null }
            }

            row {
                checkBox("Use attributes to generate the model")
                    .bindSelected(model::useAttributes)
            }

            row {
                checkBox("Use database names")
                    .bindSelected(model::useDatabaseNames)
            }

            if (efCoreVersion.major >= 5) {
                row {
                    checkBox("Generate OnConfiguring method")
                        .bindSelected(model::generateOnConfiguring)
                }

                row {
                    checkBox("Use the pluralizer")
                        .bindSelected(model::usePluralizer)
                }
            }
        }
    }

    private fun createDbContextTab(): DialogPanel = panel {
        row("Generated DbContext name:") {
            textField().bindText(model::dbContextName)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.dbContextNameValidation())
                .validationOnInput(validator.dbContextNameValidation())
        }

        row("Generated DbContext folder:") {
            textFieldForRelativeFolder(
                ::currentMigrationsProjectFolderGetter,
                intellijProject,
                "Select Generated DbContext Folder")
                .bindText(model::dbContextFolder)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.dbContextFolderValidation())
                .validationOnInput(validator.dbContextFolderValidation())
                .applyToComponent { isEnabled = commonOptions.migrationsProject != null }
        }
    }.apply(::configureValidation)

    private fun createTablesTab(): DialogPanel {
        return createToggleableTablePanel(tablesModel, "Scaffold all tables", model::scaffoldAllTables)
    }

    private fun createSchemasTab(): DialogPanel {
        return createToggleableTablePanel(schemasModel, "Scaffold all schemas", model::scaffoldAllSchemas)
    }

    private fun createToggleableTablePanel(tableModel: SimpleListTableModel, checkboxText: String,
                                           checkboxProperty: KMutableProperty0<Boolean>): DialogPanel {
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
        }.apply(::configureValidation)
    }

    //
    // Methods
    private fun configureValidation(panel: DialogPanel) {
        val disposable = Disposer.newDisposable()
        panel.registerValidators(disposable)
        Disposer.register(myDisposable, disposable)
    }

    private fun currentMigrationsProjectFolderGetter(): String {
        val currentMigrationsProject = commonOptions.migrationsProject!!.data.fullPath

        return File(currentMigrationsProject).parentFile.path
    }
}