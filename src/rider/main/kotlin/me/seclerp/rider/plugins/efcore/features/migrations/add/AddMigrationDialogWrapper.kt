package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import java.io.File
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

@Suppress("UnstableApiUsage")
class AddMigrationDialogWrapper(
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    selectedDotnetProjectName: String,
) : EfCoreDialogWrapper("Add Migration", beModel, intellijProject, selectedDotnetProjectName, false) {

    //
    // Data binding
    val model = AddMigrationModel("", "Migrations")

    //
    // Internal data
    private var availableMigrationsList = listOf<MigrationInfo>()
    private var currentDbContextMigrationsList = listOf<String>()
    private var userInputReceived: Boolean = false
    private lateinit var migrationNameTextField: JBTextField
    private var migrationsProjectSpecified: Boolean = false

    //
    // Validation
    private val validator = AddMigrationValidator()

    //
    // Constructor
    init {
        addMigrationsProjectChangedListener(::onMigrationsProjectChanged)
        addDbContextChangedListener(::onDbContextChanged)

        init()
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        createMigrationNameRow()
    }

    private fun Panel.createMigrationNameRow() {
        row("Migration name:") {
            textField()
                .bindText(model::migrationName)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput { validator.migrationNameValidation(currentDbContextMigrationsList)(it) }
                .validationOnApply { validator.migrationNameValidation(currentDbContextMigrationsList)(it) }
                .focused()
                .applyToComponent {
                    document.addDocumentListener(migrationNameChangedListener)
                    migrationNameTextField = this
                }
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options"){
            row("Migrations folder:") {
                textFieldForRelativeFolder(
                    { currentMigrationsProjectFolderGetter() },
                    intellijProject,
                    "Select Migrations Folder")
                    .bindText(model::migrationsOutputFolder)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput(validator.migrationsOutputFolderValidation())
                    .validationOnApply(validator.migrationsOutputFolderValidation())
                    .applyToComponent {
                        isEnabled = migrationsProjectSpecified
                    }
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
        enableOutputFolderField()
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
        val migrationName = if (migrationsExist) "" else "Initial"

        migrationNameTextField.document.removeDocumentListener(migrationNameChangedListener)
        migrationNameTextField.text = migrationName
        migrationNameTextField.document.addDocumentListener(migrationNameChangedListener)
    }

    private fun currentMigrationsProjectFolderGetter(): String {
        val currentMigrationsProject = commonOptions.migrationsProject!!.data.fullPath

        return File(currentMigrationsProject).parentFile.path
    }

    private fun enableOutputFolderField() {
        migrationsProjectSpecified = false
    }
}