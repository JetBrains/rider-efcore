package com.jetbrains.rider.plugins.efcore.v2

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.v2.dialogs.AddMigrationDialog
import com.jetbrains.rider.plugins.efcore.v2.dialogs.CommandDialog

@Service(Service.Level.PROJECT)
class CommandDialogFactory {
    companion object {
        fun getInstance(project: Project) = project.service<CommandDialogFactory>()
    }

    fun createDialog(type: CommandDialogType): CommandDialog = when (type) {
        CommandDialogType.ADD_MIGRATION -> AddMigrationDialog()
    }
}

enum class CommandDialogType {
    ADD_MIGRATION,
    REMOVE_LAST_MIGRATION,
    GENERATE_SQL_SCRIPT,
    UPDATE_DATABASE,
    DROP_DATABASE,
}
