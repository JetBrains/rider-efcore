package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rd.ide.model.ProjectInfo
import com.jetbrains.rd.ide.model.RiderEfCoreModel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import javax.swing.JCheckBox

class UpdateDatabaseDialogWrapper(
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Update Database", model, intellijProject, currentDotnetProjectName, true) {

    var targetMigration: String = ""
        private set

    var useDefaultConnection: Boolean = true
        private set

    var connection: String = ""
        private set

    private val availableMigrationsList = mutableListOf<String>()

    private lateinit var useDefaultConnectionCheckbox: JCheckBox

    init {
        migrationsProjectChangedEvent += ::refreshCompletion
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this) {
                targetMigrationRow(this)
            }
            additionalOptions(this) {
                row {
                    useDefaultConnectionCheckbox = checkBox("Use default connection of startup project", ::useDefaultConnection).component
                    row("Connection:") {
                        textField(::connection)
//                            .withValidationOnApply(connectionValidation())
                    }.enableIf(useDefaultConnectionCheckbox.selected.not())
                }
            }
        }
    }

    private fun targetMigrationRow(parent: LayoutBuilder): Row {
        val completionItemsIcon = DotnetIconResolver.resolveForExtension("cs")
        val provider = TextFieldWithAutoCompletion.StringsCompletionProvider(availableMigrationsList, completionItemsIcon)
        val targetMigrationTextField = TextFieldWithCompletion(intellijProject, provider, "Initial", true, true, false, false)
        targetMigrationTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                targetMigration = event.document.text
            }
        })

        return parent.row("Target migration:") {
            targetMigrationTextField(CCFlags.pushX, CCFlags.growX)
                .focused()
                .withValidationOnApply(targetMigrationValidation())
        }
    }

    private fun targetMigrationValidation(): ValidationInfoBuilder.(TextFieldWithCompletion) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Target migration could not be empty")
        else if (!availableMigrationsList.contains(it.text))
            warning("Migration with such name doesn't exist")
        else null
    }

    private fun connectionValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Connection could not be empty")
        else null
    }

    private fun refreshCompletion(migrationsProject: ProjectInfo) {
        val migrations = model.getAvailableMigrations.runUnderProgress(migrationsProject.name, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )
        availableMigrationsList.clear()
        availableMigrationsList.addAll(0, migrations!!.map { it.longName })
    }
}