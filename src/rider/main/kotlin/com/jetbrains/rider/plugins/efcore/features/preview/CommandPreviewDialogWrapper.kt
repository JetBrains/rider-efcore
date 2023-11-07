package com.jetbrains.rider.plugins.efcore.features.preview

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.ui.readonlyExpandableTextField
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class CommandPreviewDialogWrapper(
    private val command: CliCommand,
) : DialogWrapper(true) {
    init {
        init()

        title = EfCoreUiBundle.message("dialog.title.command.preview")
        window.minimumSize = Dimension(500, 200)
    }

    override fun createCenterPanel(): JComponent = panel {
        row(EfCoreUiBundle.message("working.directory")) {
            readonlyExpandableTextField { command.commandLine.workDirectory.path }
                .align(AlignX.FILL)
        }

        row(EfCoreUiBundle.message("command")) {
            readonlyExpandableTextField { command.commandLine.commandLineString }
                .align(AlignX.FILL)
        }
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)
}