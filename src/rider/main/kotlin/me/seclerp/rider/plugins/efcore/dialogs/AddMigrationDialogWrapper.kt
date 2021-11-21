package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.titledRow
import com.jetbrains.rd.ui.bedsl.dsl.combobox
import com.jetbrains.rd.util.lifetime.Lifetime
import javax.swing.JTextField

class AddMigrationDialogWrapper(
    private val lifetime: Lifetime,
    private val currentProjectName: String,
    private val projectNames: Array<String>) : DialogWrapper(true) {

    var migrationName = ""
        private set

    var migrationsProject = currentProjectName
        private set

    var startupProject = currentProjectName
        private set

    var noBuild = false
        private set

    init {
        title = "Add Migration"
//        setSize(460, 200)
//        isResizable = false

        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row("Migration name:") {
                textField(::migrationName).focused()
                    .withValidationOnInput(migrationNameValidation())
                    .withValidationOnApply(migrationNameValidation())
            }
            row("Migrations project:") {
                cell(isFullWidth = true) {
                    comboBox(projectNames, currentProjectName, handleSelected = { migrationsProject = it })()
                }
            }
            row("Startup project:") {
                cell(isFullWidth = true) {
                    comboBox(projectNames, currentProjectName, handleSelected = { startupProject = it })()
                }
            }
            titledRow("Additional") {
                row {
                    checkBox("Skip project build process (--no-build)", ::noBuild)
                }
            }
        }
    }

    private fun migrationNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.isEmpty()) {
            error("Migration name could not be empty")
        } else {
            null
        }
    }
}