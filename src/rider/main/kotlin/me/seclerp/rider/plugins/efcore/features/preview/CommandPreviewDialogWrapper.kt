package me.seclerp.rider.plugins.efcore.features.preview

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.ui.readonlyExpandableTextField
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class CommandPreviewDialogWrapper(
    private val cliCommand: CliCommand,
) : DialogWrapper(true) {
    init {
        init()

        title = "Command Preview"
        window.minimumSize = Dimension(500, 200)
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Working directory:") {
            readonlyExpandableTextField({ cliCommand.workingDirectory })
                .horizontalAlign(HorizontalAlign.FILL)
        }

        row("Command:") {
            readonlyExpandableTextField({ cliCommand.commandText })
                .horizontalAlign(HorizontalAlign.FILL)
        }
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)
}