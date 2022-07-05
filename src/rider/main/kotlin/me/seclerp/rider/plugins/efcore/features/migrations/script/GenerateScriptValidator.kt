package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem

class GenerateScriptValidator {
    fun outputFileValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Script output file could not be empty")
        else
            null
    }

    fun fromMigrationValidation(): ValidationInfoBuilder.(ComboBox<MigrationItem>) -> ValidationInfo? = {
        if (it.selectedItem == null)
            error("From migration should be specified")
        else
            null
    }
}