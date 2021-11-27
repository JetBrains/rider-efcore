package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import javax.swing.JTextField

class AddMigrationDialogWrapper(
    intellijProject: Project,
    currentProject: ProjectInfo,
    migrationsProjects: Array<ProjectInfo>,
    startupProjects: Array<ProjectInfo>
) : BaseEfCoreDialogWrapper("Add Migration", intellijProject, currentProject, migrationsProjects, startupProjects) {

    var migrationName = ""
        private set

    init {
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
        else
            null
    }
}