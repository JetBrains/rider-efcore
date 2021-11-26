package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.JTextField

class AddMigrationDialogWrapper(
    currentProjectName: String,
    migrationsProjects: Array<String>,
    startupProjects: Array<String>
) : EfCoreCommandDialogWrapper("Add Migration", currentProjectName, migrationsProjects, startupProjects) {

    var migrationName = ""
        private set

    init {
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this) {
                migrationNameRow(it)
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
        else
            null
    }
}