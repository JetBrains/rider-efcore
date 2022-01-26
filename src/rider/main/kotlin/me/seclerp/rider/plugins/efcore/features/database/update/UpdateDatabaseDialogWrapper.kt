package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.ui.textFieldWithCompletion

@Suppress("UnstableApiUsage")
class UpdateDatabaseDialogWrapper(
    private val efCoreVersion: DotnetEfVersion,
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    selectedDotnetProjectName: String
) : EfCoreDialogWrapper("Update Database", beModel, intellijProject, selectedDotnetProjectName, true) {

    //
    // Data binding
    val model = UpdateDatabaseModel("", true, "")

    //
    // Internal data
    private var availableMigrationsList = listOf<MigrationInfo>()
    private val currentDbContextMigrationsList = mutableListOf<String>()
    private lateinit var targetMigrationTextField: TextFieldWithCompletion

    //
    // Validation
    private val validator = UpdateDatabaseValidator(currentDbContextMigrationsList)

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
        createTargetMigrationRow()
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options") {
            if (efCoreVersion.major >= 5) {
                var useDefaultConnectionCheckbox: JBCheckBox? = null
                row {
                    useDefaultConnectionCheckbox =
                        checkBox("Use default connection of startup project")
                            .bindSelected(model::useDefaultConnection)
                            .component
                }
                row("Connection:") {
                    textField()
                        .bindText(model::connection)
                        .validationOnInput(validator.connectionValidation())
                        .validationOnApply(validator.connectionValidation())
                        .enabledIf(useDefaultConnectionCheckbox!!.selected.not())
                }
            }
        }
    }

    private fun Panel.createTargetMigrationRow() {
        row("Target migration:") {
            createTargetMigrationField()
                .horizontalAlign(HorizontalAlign.FILL)
                .comment("Use '0' as a target migration to undo all applied migrations")
                .focused()
                .validationOnInput(validator.targetMigrationValidation())
                .validationOnApply(validator.targetMigrationValidation())
        }
    }

    private fun Row.createTargetMigrationField(): Cell<TextFieldWithCompletion> =
        textFieldWithCompletion(model::targetMigration, currentDbContextMigrationsList, intellijProject, completionItemsIcon)
            .applyToComponent { targetMigrationTextField = this }

    //
    // Event listeners
    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        if (commonOptions.dbContext == null) {
            refreshCurrentDbContextMigrations(null)
            return
        }

        val migrationsIdentity = MigrationsIdentity(
            migrationsProjectItem.displayName,
            commonOptions.dbContext!!.data)

        availableMigrationsList = beModel.getAvailableMigrations
            .runUnderProgress(migrationsIdentity, intellijProject, "Loading migrations...",
                isCancelable = true,
                throwFault = true
            )?.sortedByDescending { it.migrationLongName } ?: listOf()

        refreshCurrentDbContextMigrations(commonOptions.dbContext)
    }

    private fun onDbContextChanged(dbContext: DbContextItem?) {
        refreshCurrentDbContextMigrations(dbContext)
    }

    private fun refreshCurrentDbContextMigrations(dbContext: DbContextItem?) {
        currentDbContextMigrationsList.clear()

        if (dbContext == null) {
            return
        }

        val availableDbContextMigrations = availableMigrationsList
            .filter { it.dbContextClassFullName == dbContext.data }
            .map { it.migrationLongName }

        if (availableDbContextMigrations.isEmpty()) {
            model.targetMigration = ""
        } else {
            val lastMigration = availableDbContextMigrations.first()
            model.targetMigration = lastMigration
            currentDbContextMigrationsList.addAll(0, availableDbContextMigrations)
        }

        currentDbContextMigrationsList.add("0")

        targetMigrationTextField.text = model.targetMigration
    }

    companion object {
        val completionItemsIcon = DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS)
    }
}