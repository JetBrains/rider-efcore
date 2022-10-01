package me.seclerp.rider.plugins.efcore.features.preview

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.rider.plugins.efcore.ui.readonlyExpandableTextField
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class CommandPreviewDialogWrapper(
    private val cliCommand: GeneralCommandLine,
) : DialogWrapper(true) {
    init {
        init()

        title = "Command Preview"
        window.minimumSize = Dimension(500, 200)
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Working directory:") {
            readonlyExpandableTextField { cliCommand.workDirectory.path }
                .horizontalAlign(HorizontalAlign.FILL)
        }

        row("Command:") {
            readonlyExpandableTextField { cliCommand.commandLineString }
                .horizontalAlign(HorizontalAlign.FILL)
        }
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)
}