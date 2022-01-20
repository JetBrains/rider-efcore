package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold.v2

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.ui.table.JBTable
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem
import me.seclerp.rider.plugins.efcore.ui.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.features.shared.v2.EfCoreDialogWrapper
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

@Suppress("UnstableApiUsage")
class ScaffoldDatabaseDialogWrapper(
    private val efCoreVersion: DotnetEfVersion,
    beModel: RiderEfCoreModel,
    intellijProject: Project,
    currentDotnetProjectName: String,
) : EfCoreDialogWrapper("Scaffold Database", beModel, intellijProject, currentDotnetProjectName, false) {

    //
    // Data binding
    val model = ScaffoldDatabaseModel(
        connection = "",
        provider = "",
        outputFolder = "Models",
        useAttributes = false,
        useDatabaseNames = false,
        generateOnConfiguring = true,
        usePluralizer = true,
        dbContextName = "MyDbContext",
        dbContextFolder = "DbContext",
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
    private val validator = ScaffoldDatabaseValidator()

    //
    // Constructor
    init {
        init()
    }

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

        return tabbedPane
    }

//    override fun doOKAction() {
//        if (okAction.isEnabled) {
//            mainTab.apply()
//            dbContextTab.apply()
//            tablesTab.apply()
//            schemaTab.apply()
//        }
//
//        super.doOKAction()
//    }

    private fun createMainTab(): DialogPanel =
        createMainUI()
            .apply(::configureValidation)

    override fun createPrimaryOptions(): Panel.() -> Panel = {
        panel {
            row("Connection:") {
                textField().bindText(model::connection)
                    .validationOnInput(validator.connectionValidation())
                    .validationOnApply(validator.connectionValidation())
            }

            row("Provider:") {
                textField().bindText(model::provider)
                    .validationOnInput(validator.providerValidation())
                    .validationOnApply(validator.providerValidation())
            }
        }
    }

    override fun createAdditionalOptions(): Panel.() -> Panel = {
        panel {
            row("Output folder") {
                textField().bindText(model::outputFolder)
                    .validationOnInput(validator.outputFolderValidation())
                    .validationOnApply(validator.outputFolderValidation())
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
        row("Generated DbContext name") {
            textField().bindText(model::dbContextName)
                .validationOnInput(validator.dbContextNameValidation())
                .validationOnInput(validator.dbContextNameValidation())
        }

        row("Generated DbContext folder") {
            textField().bindText(model::dbContextFolder)
                .validationOnInput(validator.dbContextFolderValidation())
                .validationOnInput(validator.dbContextFolderValidation())
        }
    }.apply(::configureValidation)

    private fun createTablesTab(): DialogPanel {
        return createToggleableTablePanel(tablesModel, "Scaffold all tables", model::scaffoldAllTables)
    }

    private fun createSchemasTab(): DialogPanel {
        return createToggleableTablePanel(schemasModel, "Scaffold all schemas", model::scaffoldAllSchemas)
    }

    private fun createToggleableTablePanel(model: SimpleListTableModel, checkboxText: String,
                                           checkboxProperty: KMutableProperty0<Boolean>): DialogPanel {
        val table = JBTable(model)
            .apply {
                tableHeader.isVisible = false
            }

        val tableDecorator = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(SimpleItem(""))
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

            row {
                cell(tablePanel)
                    .enabledIf(enabledCheckbox!!.selected.not())
            }
        }.apply(::configureValidation)
    }

    private fun configureValidation(panel: DialogPanel) {
        val disposable = Disposer.newDisposable()
        panel.registerValidators(disposable)
        Disposer.register(myDisposable, disposable)
    }
//
//    // TODO: Remove after workaround for nested panels binding will be available
//    private fun customTextField(getter: () -> String, setter: (String) -> Unit): JBTextField {
//        val textField = JBTextField(getter())
//        textField.document.addDocumentListener(object : DocumentListener {
//            override fun insertUpdate(event: DocumentEvent?) {
//                handleChanged(textField.document.getText(0, textField.document.length))
//            }
//
//            override fun removeUpdate(event: DocumentEvent?) {
//                handleChanged(textField.document.getText(0, textField.document.length))
//            }
//
//            override fun changedUpdate(event: DocumentEvent?) {
//                handleChanged(textField.document.getText(0, textField.document.length))
//            }
//
//            private fun handleChanged(newValue: String) {
//                setter(newValue)
//            }
//        })
//
//        return textField
//    }
}