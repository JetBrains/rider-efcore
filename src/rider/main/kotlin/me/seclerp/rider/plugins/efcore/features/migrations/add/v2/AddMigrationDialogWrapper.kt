package me.seclerp.rider.plugins.efcore.features.migrations.add.v2

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.features.shared.v2.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

@Suppress("UnstableApiUsage")
class AddMigrationDialogWrapper(
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val selectedDotnetProjectName: String,
) : EfCoreDialogWrapper("Add Migration", beModel, intellijProject, selectedDotnetProjectName, false) {

    //
    // Data binding
    val model = AddMigrationModel("")

    //
    // Internal data
    private var availableMigrationsList = listOf<MigrationInfo>()
    private var currentDbContextMigrationsList = listOf<String>()
    private var userInputReceived: Boolean = false
    private lateinit var migrationNameTextField: JBTextField

    //
    // Validation
    val validator = AddMigrationValidator()

    //
    // Constructor
    init {
        addMigrationsProjectChangedListener(::onMigrationsProjectChanged)
        addDbContextChangedListener(::onDbContextChanged)

        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            createMigrationNameRow()(this)
            createPrimaryGroup()(this)
            createSecondaryGroup()(this)
        }

    private fun createMigrationNameRow(): Panel.() -> Row = {
        row {
            textField()
                .bindText(model::migrationName)
                .validationOnInput { validator.migrationNameValidation(currentDbContextMigrationsList)(it) }
                .validationOnApply { validator.migrationNameValidation(currentDbContextMigrationsList)(it) }
                .focused()
                .applyToComponent {
                    document.addDocumentListener(migrationNameChangedListener)
                    migrationNameTextField = this
                }
        }
    }

    //
    // Event listeners
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

    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        refreshAvailableMigrations(migrationsProjectItem.displayName)
        refreshCurrentDbContextMigrations(commonOptions.dbContext)
    }

    private fun onDbContextChanged(dbContext: DbContextItem?) {
        refreshCurrentDbContextMigrations(dbContext)
    }

    //
    // Methods
    private fun refreshAvailableMigrations(migrationsProjectName: String) {
        availableMigrationsList = beModel.getAvailableMigrations.runUnderProgress(migrationsProjectName, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )!!
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