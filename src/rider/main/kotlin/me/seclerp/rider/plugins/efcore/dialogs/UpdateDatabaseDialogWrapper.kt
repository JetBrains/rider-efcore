package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.options.ex.ConfigurableExtensionPointUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.layout.*
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rd.ide.model.RiderEfCoreModel
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.DotnetIconResolver

class UpdateDatabaseDialogWrapper(
    private val project: Project,
    private val model: RiderEfCoreModel,
    currentProjectName: String,
    migrationsProjects: Array<String>,
    startupProjects: Array<String>
) : EfCoreCommandDialogWrapper("Update Database", currentProjectName, migrationsProjects, startupProjects) {

    var targetMigration: String = ""
        private set

    private val availableMigrationsList = mutableListOf<String>()

    init {
        migrationsProjectNameChangedEvent += ::refreshCompletion
        super.init()
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this) {
                targetMigrationRow(it)
            }
            additionalOptions(this)
        }
    }

    private fun targetMigrationRow(it: LayoutBuilder): Row {
        val completionItemsIcon = DotnetIconResolver.resolveForExtension("cs")
        val provider = TextFieldWithAutoCompletion.StringsCompletionProvider(availableMigrationsList, completionItemsIcon)
        val textFieldWithCompletion = TextFieldWithCompletion(project, provider, "Initial", true, true, false, false)
        textFieldWithCompletion.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                targetMigration = event.document.text
            }
        })

        return it.row("Target migration") {
            textFieldWithCompletion(CCFlags.pushX, CCFlags.growX)
                .focused()
                .withValidationOnApply(targetMigrationValidation())
                .withValidationOnInput(targetMigrationValidation())
        }
    }

    private fun targetMigrationValidation(): ValidationInfoBuilder.(TextFieldWithCompletion) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Target migration could not be empty")
        else if (!availableMigrationsList.contains(it.text))
            warning("Migration with such name doesn't exist")
        else null
    }

    private fun refreshCompletion(migrationsProject: String) {
        val migrations = model.getAvailableMigrations.runUnderProgress(migrationsProject, project, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        );
        availableMigrationsList.clear()
        availableMigrationsList.addAll(0, migrations!!)
    }
}