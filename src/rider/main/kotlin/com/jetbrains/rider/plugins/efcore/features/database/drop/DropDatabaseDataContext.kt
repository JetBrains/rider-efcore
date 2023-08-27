package com.jetbrains.rider.plugins.efcore.features.database.drop

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.cli.api.DatabaseCommandFactory
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class DropDatabaseDataContext(intellijProject: Project) : CommonDataContext(intellijProject, true, false) {
    private val databaseCommandFactory = intellijProject.service<DatabaseCommandFactory>()

    override fun generateCommand(): GeneralCommandLine {
        val options = getCommonOptions()
        return databaseCommandFactory.drop(options)
    }
}