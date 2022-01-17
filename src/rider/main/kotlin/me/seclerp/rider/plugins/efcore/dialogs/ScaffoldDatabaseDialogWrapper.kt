package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import me.seclerp.rider.plugins.efcore.components.items.SimpleItem
import me.seclerp.rider.plugins.efcore.components.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.models.EfCoreVersion
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import java.awt.Component
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.reflect.KMutableProperty0

class ScaffoldDatabaseDialogWrapper(
    private val efCoreVersion: EfCoreVersion,
    model: RiderEfCoreModel,
    intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Scaffold Database", model, intellijProject, currentDotnetProjectName, false) {
    var connection: String = ""
    var provider: String = ""
    var outputFolder: String = "Models"

    var useAttributes: Boolean = false
    var useDatabaseNames: Boolean = false
    var generateOnConfiguring: Boolean = true
    var usePluralizer: Boolean = true

    var dbContextName: String = "MyDbContext"
    var dbContextFolder: String = "Context"

    val tablesList = mutableListOf<SimpleItem>()
    private val tablesModel = SimpleListTableModel(tablesList)

    val schemasList = mutableListOf<SimpleItem>()
    private val schemasModel = SimpleListTableModel(schemasList)

    var scaffoldAllTables: Boolean = true
    var scaffoldAllSchemas: Boolean = true

    private lateinit var mainTab: DialogPanel
    private lateinit var dbContextTab: DialogPanel
    private lateinit var tablesTab: DialogPanel
    private lateinit var schemaTab: DialogPanel


    init {
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()

        mainTab = createMainTab()
        dbContextTab = createDbContextTab()
        tablesTab = createTablesTab()
        schemaTab = createSchemasTab()

        tabbedPane.addTab("Main", mainTab)
        tabbedPane.addTab("DbContext", dbContextTab)
        tabbedPane.addTab("Tables", tablesTab)
        tabbedPane.addTab("Schemas", schemaTab)
//        val panel = DialogPanel(BorderLayout())
//        panel.add(tabbedPane, BorderLayout.CENTER)

        return panel {
            row {
                tabbedPane()
                    .constraints(CCFlags.pushX, CCFlags.growX, CCFlags.pushY, CCFlags.growY)
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

    private fun createMainTab(): DialogPanel = panel {
        migrationsProjectRow(this)
        startupProjectRow(this)

        row("Connection") {
            customTextField({ connection }, { connection = it })()
                .withValidationOnApply(connectionValidation())
                .withValidationOnInput(connectionValidation())
        }

        row("Provider") {
            customTextField({ provider }, { provider = it })()
                .withValidationOnApply(providerValidation())
                .withValidationOnInput(providerValidation())
        }

        additionalOptions(this) {
            row("Output folder") {
                customTextField({ outputFolder }, { outputFolder = it })()
                    .withValidationOnApply(outputFolderValidation())
                    .withValidationOnInput(outputFolderValidation())
            }

            row {
                checkBox("Use attributes to generate the model", ::useAttributes)
            }

            row {
                checkBox("Use database names", ::useDatabaseNames)
            }

            if (efCoreVersion.major >= 5) {
                row {
                    checkBox("Generate OnConfiguring method", ::generateOnConfiguring)
                }

                row {
                    checkBox("Use the pluralizer", ::usePluralizer)
                }
            }
        }
    }.apply {
        this.registerValidators(myDisposable) {
            isOKActionEnabled = it.isEmpty()
        }
    }

    private fun connectionValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Connection could not be empty")
        else
            null
    }

    private fun providerValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Provider could not be empty")
        else
            null
    }

    private fun outputFolderValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Output folder could not be empty")
        else
            null
    }

    private fun createDbContextTab(): DialogPanel = panel {
        row("Generated DbContext name") {
            customTextField({ dbContextName }, { dbContextName = it })()
                .withValidationOnApply(dbContextNameValidation())
                .withValidationOnInput(dbContextNameValidation())
        }

        row("Generated DbContext folder") {
            customTextField({ dbContextFolder }, { dbContextFolder = it })()
                .withValidationOnApply(dbContextFolderValidation())
                .withValidationOnInput(dbContextFolderValidation())
        }
    }.apply {
        this.registerValidators(myDisposable) {
            isOKActionEnabled = it.isEmpty()
            this.apply()
        }
    }

    private fun dbContextNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("DbContext class name could not be empty")
        else
            null
    }

    private fun dbContextFolderValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("DbContext folder could not be empty")
        else
            null
    }

    private fun createTablesTab(): DialogPanel {
        return createToggleableTablePanel(tablesModel, "Scaffold all tables", ::scaffoldAllTables)
    }

    private fun createSchemasTab(): DialogPanel {
        return createToggleableTablePanel(schemasModel, "Scaffold all schemas", ::scaffoldAllSchemas)
    }

    private fun createToggleableTablePanel(model: SimpleListTableModel, checkboxText: String,
                                           checkboxProperty: KMutableProperty0<Boolean>): DialogPanel {
        val table = JBTable(model)
            .apply { this.tableHeader.isVisible = false }

        val tableDecorator = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(SimpleItem(""))
            }
            .setRemoveAction {
                TableUtil.removeSelectedItems(table)
            }

        val tablePanel = tableDecorator.createPanel()

        return panel {
            row {
                checkBox(checkboxText, checkboxProperty)
                    .applyToComponent {
                        this.addChangeListener {
                            table.isEnabled = !this.isSelected
                            tablePanel.updateUI()
                        }
                    }
            }

            row {
                tablePanel()
                    .constraints(CCFlags.pushX, CCFlags.growX, CCFlags.pushY, CCFlags.growY)
            }
        }.apply {
            this.registerValidators(myDisposable) {
                isOKActionEnabled = it.isEmpty()
            }
        }
    }

    // TODO: Remove after workaround for nested panels binding will be available
    private fun customTextField(getter: () -> String, setter: (String) -> Unit): JBTextField {
        val textField = JBTextField(getter())
        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent?) {
                handleChanged(textField.document.getText(0, textField.document.length))
            }

            override fun removeUpdate(event: DocumentEvent?) {
                handleChanged(textField.document.getText(0, textField.document.length))
            }

            override fun changedUpdate(event: DocumentEvent?) {
                handleChanged(textField.document.getText(0, textField.document.length))
            }

            private fun handleChanged(newValue: String) {
                setter(newValue)
            }
        })

        return textField
    }
}