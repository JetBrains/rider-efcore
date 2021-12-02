package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.components.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AddMigrationDialogWrapper(
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Add Migration", model, intellijProject, currentDotnetProjectName, false) {

    var migrationName = ""
        private set

    private var existedMigrations: List<String> = listOf()
    private var userInputReceived: Boolean = false

    private lateinit var migrationNameTextField: JBTextField

    private val migrationNameChangedListener = object : DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) {
            userInputReceived = true
        }

        override fun insertUpdate(e: DocumentEvent?) {
            userInputReceived = true
        }

        override fun removeUpdate(e: DocumentEvent?) {
            userInputReceived = true
        }
    }

    init {
        migrationsProjectChangedEvent += ::onMigrationsProjectChanged
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this) {
                migrationNameRow(this)
            }

            additionalOptions(this)
        }
    }

    private fun migrationNameRow(it: LayoutBuilder) =
        it.row("Migration name:") {
            migrationNameTextField = textField(::migrationName)
                .constraints(CCFlags.pushX, CCFlags.growX)
                .focused()
                .withValidationOnInput(migrationNameValidation())
                .withValidationOnApply(migrationNameValidation())
                .component

            migrationNameTextField.document.addDocumentListener(migrationNameChangedListener)
        }

    private fun migrationNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Migration name could not be empty")
        else if (existedMigrations.contains(it.text.trim()))
            error("Migration with such name already exist")
        else
            null
    }

    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        refreshMigrations(migrationsProjectItem.displayName)
        setInitialMigrationNameIfNeeded()
    }

    private fun refreshMigrations(migrationsProjectName: String) {
        existedMigrations = model.getAvailableMigrations.runUnderProgress(migrationsProjectName, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )!!.map { it.shortName }
    }

    private fun setInitialMigrationNameIfNeeded() {
        if (userInputReceived) return

        val migrationsExist = existedMigrations.isNotEmpty()

        migrationNameTextField.document.removeDocumentListener(migrationNameChangedListener)
        migrationNameTextField.text = if (migrationsExist) "" else "Initial"
        migrationNameTextField.document.addDocumentListener(migrationNameChangedListener)
    }
}