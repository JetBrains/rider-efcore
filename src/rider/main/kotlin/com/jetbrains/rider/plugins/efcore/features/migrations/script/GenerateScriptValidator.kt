package com.jetbrains.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.ui.items.MigrationItem

class GenerateScriptValidator {
    fun outputFileValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.script.output.file.could.not.be.empty"))
        else
            null
    }

    fun fromMigrationValidation(): ValidationInfoBuilder.(ComboBox<MigrationItem>) -> ValidationInfo? = {
        if (it.selectedItem == null)
            error(EfCoreUiBundle.message("dialog.message.from.migration.should.be.specified"))
        else
            null
    }
}