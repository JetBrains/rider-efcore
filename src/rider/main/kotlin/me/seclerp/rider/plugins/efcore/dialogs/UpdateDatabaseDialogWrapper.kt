package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.layout.*
import com.intellij.util.textCompletion.TextFieldWithCompletion
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType
import me.seclerp.rider.plugins.efcore.components.items.DbContextItem
import me.seclerp.rider.plugins.efcore.components.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
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

    private var availableMigrationsList = listOf<MigrationInfo>()
    private val currentDbContextMigrationsList = mutableListOf<String>()

    private lateinit var useDefaultConnectionCheckbox: JCheckBox
    private lateinit var targetMigrationTextField: TextFieldWithCompletion

    init {
        migrationsProjectChangedEvent += ::onMigrationsProjectChanged
        dbContextChangedEvent += ::refreshCurrentDbContextMigrations
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
        val completionItemsIcon = DotnetIconResolver.resolveForType(DotnetIconType.CLASS)
        val provider = TextFieldWithAutoCompletion.StringsCompletionProvider(currentDbContextMigrationsList, completionItemsIcon)
        targetMigrationTextField = TextFieldWithCompletion(intellijProject, provider, "Initial", true, true, false, false)
        targetMigrationTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                targetMigration = event.document.text
            }
        })

        return parent.row("Target migration:") {
            targetMigrationTextField(CCFlags.pushX, CCFlags.growX)
                .comment("Use '0' as a target migration to undo all applied migrations")
                .focused()
                .withValidationOnApply(targetMigrationValidation())
        }
    }

    private fun targetMigrationValidation(): ValidationInfoBuilder.(TextFieldWithCompletion) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Target migration could not be empty")
        else if (!currentDbContextMigrationsList.contains(it.text))
            error("Migration with such name doesn't exist")
        else null
    }

    // TODO: Investigate why validation not worked properly for disabled fields

    //    private fun connectionValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
    //        if (it.text.isEmpty())
    //            error("Connection could not be empty")
    //        else null
    //    }

    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        availableMigrationsList = model.getAvailableMigrations.runUnderProgress(migrationsProjectItem.displayName, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )?.sortedByDescending { it.longName } ?: listOf()

        refreshCurrentDbContextMigrations(dbContext)
    }

    private fun refreshCurrentDbContextMigrations(dbContext: DbContextItem?) {
        currentDbContextMigrationsList.clear()

        if (dbContext == null) {
            return
        }

        val availableDbContextMigrations = availableMigrationsList
            .filter { it.dbContextClass == dbContext!!.data }
            .map { it.longName }

        if (availableDbContextMigrations.isEmpty())
            targetMigration = ""
        else {
            val lastMigration = availableDbContextMigrations.last()
            targetMigration = lastMigration
            currentDbContextMigrationsList.addAll(0, availableDbContextMigrations)
        }

        currentDbContextMigrationsList.add("0")

        targetMigrationTextField.text = targetMigration
    }
}