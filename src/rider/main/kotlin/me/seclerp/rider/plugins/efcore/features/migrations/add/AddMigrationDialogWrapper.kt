package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreDialogWrapper
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AddMigrationDialogWrapper(
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : EfCoreDialogWrapper("Add Migration", model, intellijProject, currentDotnetProjectName, false) {

    var migrationName = ""
        private set

    private var availableMigrationsList = listOf<MigrationInfo>()
    private var currentDbContextMigrationsList = listOf<String>()
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
        dbContextChangedEvent += ::onDbContextChanged
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
        else if (currentDbContextMigrationsList.contains(it.text.trim()))
            error("Migration with such name already exist")
        else
            null
    }

    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        refreshAvailableMigrations(migrationsProjectItem.displayName)
        refreshCurrentDbContextMigrations(dbContext)
    }

    private fun refreshAvailableMigrations(migrationsProjectName: String) {
        availableMigrationsList = model.getAvailableMigrations.runUnderProgress(migrationsProjectName, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )!!
    }

    private fun onDbContextChanged(dbContext: DbContextItem?) {
        refreshCurrentDbContextMigrations(dbContext)
    }

    private fun refreshCurrentDbContextMigrations(dbContext: DbContextItem?) {
        currentDbContextMigrationsList =
            if (dbContext == null)
                listOf()
            else
                availableMigrationsList
                    .filter { it.dbContextClass == dbContext.data }
                    .map { it.shortName }
                    .toList()

        setInitialMigrationNameIfNeeded()
    }

    private fun setInitialMigrationNameIfNeeded() {
        if (userInputReceived) return

        val migrationsExist = currentDbContextMigrationsList.isNotEmpty()

        migrationNameTextField.document.removeDocumentListener(migrationNameChangedListener)
        migrationNameTextField.text = if (migrationsExist) "" else "Initial"
        migrationNameTextField.document.addDocumentListener(migrationNameChangedListener)
    }
}