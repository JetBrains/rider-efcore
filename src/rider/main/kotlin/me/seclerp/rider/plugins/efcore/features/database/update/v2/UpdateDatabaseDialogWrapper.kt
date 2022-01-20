package me.seclerp.rider.plugins.efcore.features.database.update.v2

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.v2.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
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
    private val selectedDotnetProjectName: String
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

    override fun createPrimaryOptions(): Panel.() -> Panel = {
        panel {
            createTargetMigrationRow()(this)
        }
    }

    override fun createAdditionalOptions(): Panel.() -> Panel = {
        group("Additional Options") {
            if (efCoreVersion.major >= 5) {
                indent {
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
                    }.enabledIf(useDefaultConnectionCheckbox!!.selected.not())
                }
            }
        }
    }

    private fun createTargetMigrationRow(): Panel.() -> Row = {
        row("Target migration:") {
            createTargetMigrationField()(this)
                .horizontalAlign(HorizontalAlign.FILL)
                .comment("Use '0' as a target migration to undo all applied migrations")
                .focused()
                .validationOnInput(validator.targetMigrationValidation())
                .validationOnApply(validator.targetMigrationValidation())
        }
    }

    private fun createTargetMigrationField(): Row.() -> Cell<JBTextField> = {
        textFieldWithCompletion(model::targetMigration, currentDbContextMigrationsList, completionItemsIcon)
    }

    //
    // Event listeners
    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        availableMigrationsList = beModel.getAvailableMigrations
            .runUnderProgress(migrationsProjectItem.displayName, intellijProject, "Loading migrations...",
                isCancelable = true,
                throwFault = true
            )?.sortedByDescending { it.longName } ?: listOf()

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
            .filter { it.dbContextClass == dbContext.data }
            .map { it.longName }

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