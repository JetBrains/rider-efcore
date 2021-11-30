package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.components.items.IconItem
import me.seclerp.rider.plugins.efcore.components.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import javax.swing.JTextField

class AddMigrationDialogWrapper(
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Add Migration", model, intellijProject, currentDotnetProjectName, false) {

    var migrationName = ""
        private set

    private var existedMigrations: List<String> = listOf()

    init {
        migrationsProjectChangedEvent += ::refreshMigrations
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
            textField(::migrationName)
                .constraints(CCFlags.pushX, CCFlags.growX)
                .focused()
                .withValidationOnInput(migrationNameValidation())
                .withValidationOnApply(migrationNameValidation())
        }

    private fun migrationNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Migration name could not be empty")
        else if (existedMigrations.contains(it.text.trim()))
            error("Migration with such name already exist")
        else
            null
    }

    private fun refreshMigrations(migrationsProjectItem: MigrationsProjectItem) {
        existedMigrations = model.getAvailableMigrations.runUnderProgress(migrationsProjectItem.displayName, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )!!.map { it.shortName }
    }
}