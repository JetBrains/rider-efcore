package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel


class AddMigrationDialogWrapper(private val projectNames: Array<String>) : DialogWrapper(true) {
    private var migrationName = ""

    init {
        title = "Add Migration"
        setSize(460, 200)
        isResizable = false

        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val migrationProjectBox = ComboBox(projectNames)
        return panel {
            row("Migration name:") {
                textField(::migrationName).focused()
            }
            row("Migration project:") {
                cell(isFullWidth = true) {
                    migrationProjectBox()
                }
            }
        }
    }
}