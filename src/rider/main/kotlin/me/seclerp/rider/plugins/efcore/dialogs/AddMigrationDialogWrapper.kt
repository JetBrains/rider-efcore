package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import com.jetbrains.rd.ide.model.RiderEfCoreModel
import com.jetbrains.rider.util.idea.runUnderProgress
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
        if (it.text.isEmpty())
            error("Migration name could not be empty")
        if (existedMigrations.contains(it.text.trim()))
            error("Migration with such name already exist")
        else
            null
    }

    private fun refreshMigrations(migrationsProject: ProjectInfo) {
        existedMigrations = model.getAvailableMigrations.runUnderProgress(migrationsProject.name, intellijProject, "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )!!.map { it.shortName }
    }
}