package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.*
import com.intellij.ui.table.JBTable
import me.seclerp.rider.plugins.efcore.components.items.SimpleItem
import me.seclerp.rider.plugins.efcore.components.items.SimpleListTableModel
import me.seclerp.rider.plugins.efcore.models.EfCoreVersion
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTextField
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

    init {
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()
        tabbedPane.addTab("Main", createMainTab())
        tabbedPane.addTab("DbContext", createDbContextTab())
        tabbedPane.addTab("Tables", createTablesTab())
        tabbedPane.addTab("Schemas", createSchemasTab())

        val panel = DialogPanel(BorderLayout())
        panel.add(tabbedPane, BorderLayout.CENTER)

        return panel
    }

    private fun createMainTab(): Component = panel {
        migrationsProjectRow(this)
        startupProjectRow(this)

        row("Connection") {
            textField(::connection)
                .withValidationOnApply(connectionValidation())
                .withValidationOnInput(connectionValidation())
        }

        row("Provider") {
            textField(::provider)
                .withValidationOnApply(providerValidation())
                .withValidationOnInput(providerValidation())
        }

        additionalOptions(this) {
            row("Output folder") {
                textField(::outputFolder)
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

    private fun createDbContextTab(): Component = panel {
        row("Generated DbContext name") {
            textField(::dbContextName)
                .withValidationOnApply(dbContextNameValidation())
                .withValidationOnInput(dbContextNameValidation())
        }

        row("Generated DbContext folder") {
            textField(::dbContextFolder)
                .withValidationOnApply(dbContextFolderValidation())
                .withValidationOnInput(dbContextFolderValidation())
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

    private fun createTablesTab(): Component {
        return createToggleableTablePanel(tablesModel, "Scaffold all tables", ::scaffoldAllTables)
    }

    private fun createSchemasTab(): Component {
        return createToggleableTablePanel(schemasModel, "Scaffold all schemas", ::scaffoldAllSchemas)
    }

    private fun createToggleableTablePanel(model: SimpleListTableModel, checkboxText: String,
                                           checkboxProperty: KMutableProperty0<Boolean>): JPanel {
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
        }
    }
}