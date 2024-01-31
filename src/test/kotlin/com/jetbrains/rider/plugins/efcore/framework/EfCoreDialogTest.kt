package com.jetbrains.rider.plugins.efcore.framework

import com.intellij.openapi.ui.DialogPanel
import com.jetbrains.rider.plugins.efcore.cli.api.EfCoreCliCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.ui.elements
import com.jetbrains.rider.plugins.efcore.ui.items.MigrationsProjectItem
import com.jetbrains.rider.plugins.efcore.ui.items.StartupProjectItem
import kotlin.test.assertNotNull

@Suppress("HardCodedStringLiteral", "UnstableApiUsage")
abstract class EfCoreDialogTest : EfCoreTest() {
    protected suspend fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> withDialog(dialogFactory: () -> TDialog, action: suspend TDialog.(DialogPanel, TContext) -> Unit) {
        val dialog = dialogFactory()
        val panel = dialog.requestPanel()
        dialog.action(panel, dialog.dataCtx)
    }

    fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.selectMigrationsProject(name: String): MigrationsProjectItem {
        val migrationsProjectItem = assertNotNull(
            migrationsProjectComponent?.model?.elements?.firstOrNull { it.data.name == name },
            "Migrations project with name $name wasn't found in component's available items")
        migrationsProjectComponent?.model?.selectedItem = migrationsProjectItem
        return migrationsProjectItem
    }

    fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.selectStartupProject(name: String): StartupProjectItem {
        val startupProjectItem = assertNotNull(
            startupProjectComponent?.model?.elements?.firstOrNull { it.data.name == name },
            "Startup project with name $name wasn't found in component's available items")
        startupProjectComponent?.model?.selectedItem = startupProjectItem
        return startupProjectItem
    }

    fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.assertValid() {
        requestPanel().apply()
        val errors = performValidateAll()
        val presentableErrors = errors
            .joinToString("\n") { "\t${it.component}: ${it.message}" }
            .let { "Validation summary:\n$it" }
        if (errors.isNotEmpty()) {
            logger.warn(presentableErrors)
        }
        assert(errors.isEmpty()) { "Validation should succeed" }
    }

    suspend fun <TContext : CommonDataContext, TDialog : CommonDialogWrapper<TContext>> TDialog.executeCommand(): CliCommandResult {
        assertValid()
        val command = generateCommand()
        val cliCommand = EfCoreCliCommandFactory.getInstance(project).create(command, efCoreVersion)
        val result = executeCommand(cliCommand)
        postCommandExecute(result)
        return result
    }
}