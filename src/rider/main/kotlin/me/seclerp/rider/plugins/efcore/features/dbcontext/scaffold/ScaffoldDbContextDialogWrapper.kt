package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

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
import me.seclerp.rider.plugins.efcore.cli.api.DbContextCommandFactory
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem
import me.seclerp.rider.plugins.efcore.ui.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import me.seclerp.rider.plugins.efcore.features.shared.services.PreferredProjectsManager
import java.io.File
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

@Suppress("UnstableApiUsage")
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
        overrideExisting = false,
        tablesList = mutableListOf(),
        schemasList = mutableListOf(),
        scaffoldAllTables = true,
        scaffoldAllSchemas = true
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
    // ProjectsManager
    private val preferredProjectsManager = PreferredProjectsManager(intellijProject)

    //
    // Constructor
    init {
        model.connection = preferredProjectsManager.getScaffoldString(CONNECTION_STRING)
        model.provider = preferredProjectsManager.getScaffoldString(CONTEXT_PROVIDER)
        model.outputFolder = preferredProjectsManager.getScaffoldString(OUTPUT_FOLDER)
        model.useAttributes = preferredProjectsManager.getScaffoldBoolean(USE_ATTRIBUTES)
        model.useDatabaseNames = preferredProjectsManager.getScaffoldBoolean(USE_DATABASE_NAMES)
        model.generateOnConfiguring = preferredProjectsManager.getScaffoldBoolean(GENERATE_ON_CONFIGURING)
        model.usePluralizer = preferredProjectsManager.getScaffoldBoolean(USE_PLURALIZER)
        model.dbContextName = preferredProjectsManager.getScaffoldString(CONTEXT_NAME)
        model.dbContextFolder = preferredProjectsManager.getScaffoldString(CONTEXT_FOLDER)
        model.overrideExisting = preferredProjectsManager.getScaffoldBoolean(FORCE_FILE_OVERRIDE)

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
            model.schemasList.map { it.data },
            model.overrideExisting)
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

    override fun doOKAction() {
        // Main Tab
        preferredProjectsManager.setScaffoldString(CONNECTION_STRING, model::connection.get())
        preferredProjectsManager.setScaffoldString(CONTEXT_PROVIDER, model::provider.get())

        // Additional Options
        preferredProjectsManager.setScaffoldString(OUTPUT_FOLDER, model::outputFolder.get())
        preferredProjectsManager.setScaffoldBoolean(USE_ATTRIBUTES, model::useAttributes.get())
        preferredProjectsManager.setScaffoldBoolean(USE_DATABASE_NAMES, model::useDatabaseNames.get())
        preferredProjectsManager.setScaffoldBoolean(GENERATE_ON_CONFIGURING, model::generateOnConfiguring.get())
        preferredProjectsManager.setScaffoldBoolean(USE_PLURALIZER, model::usePluralizer.get())
        preferredProjectsManager.setScaffoldBoolean(FORCE_FILE_OVERRIDE, model::overrideExisting.get())

        // DbContext Tab
        preferredProjectsManager.setScaffoldString(CONTEXT_NAME, model::dbContextName.get())
        preferredProjectsManager.setScaffoldString(CONTEXT_FOLDER, model::dbContextFolder.get())

        super.doOKAction()
    }

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

            row {
                checkBox("Override existing files")
                    .bindSelected(model::overrideExisting)
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
    }

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
        }
    }

    //
    // Methods
    private fun currentMigrationsProjectFolderGetter(): String {
        val currentMigrationsProject = commonOptions.migrationsProject!!.data.fullPath

        return File(currentMigrationsProject).parentFile.path
    }

    companion object{
        const val CONNECTION_STRING = "connectionString"
        const val CONTEXT_PROVIDER = "contextProvider"
        const val OUTPUT_FOLDER = "outputFolder"
        const val CONTEXT_NAME = "contextName"
        const val CONTEXT_FOLDER = "contextFolder"
        const val USE_ATTRIBUTES = "attributes"
        const val USE_DATABASE_NAMES = "databaseNames"
        const val GENERATE_ON_CONFIGURING = "generateOnConfiguring"
        const val USE_PLURALIZER = "pluralizer"
        const val FORCE_FILE_OVERRIDE = "override"
    }
}