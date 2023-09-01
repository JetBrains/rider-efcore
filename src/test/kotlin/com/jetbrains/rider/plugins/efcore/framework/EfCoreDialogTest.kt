package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.openapi.rd.createNestedDisposable
import com.intellij.openapi.ui.DialogPanel
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper

@Suppress("HardCodedStringLiteral", "UnstableApiUsage")
abstract class EfCoreDialogTest : EfCoreTest() {
    protected fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> withDialog(dialogFactory: () -> TDialog, action: TDialog.(DialogPanel, TContext) -> Unit) {
        project.lifetime.createNested {
            val dialog = dialogFactory()
            val panel = dialog.createPanel()
            panel.registerValidators(it.createNestedDisposable())
            dialog.action(panel, dialog.dataCtx)
        }
    }

    fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.assertValid() {
        val errors = performValidateAll()
        val presentableErrors = errors
            .joinToString("\n") { "\t${it.component}: ${it.message}" }
            .let { "Validation summary:\n$it" }
        if (errors.isNotEmpty()) {
            logger.warn(presentableErrors)
        }
        assert(errors.isEmpty()) { "Validation should succeed" }
    }

    fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.executeCommand(): CliCommandResult {
        val command = dataCtx.generateCommand()
        val result = executeCommand(command)
        postCommandExecute(result)
        return result
    }
}