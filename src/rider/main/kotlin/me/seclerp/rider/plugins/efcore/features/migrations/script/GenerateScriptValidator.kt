package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.textCompletion.TextFieldWithCompletion

class GenerateScriptValidator(
    private val currentDbContextMigrationsList: MutableList<String>
) {
    fun targetMigrationValidation(): ValidationInfoBuilder.(TextFieldWithCompletion) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Target migration could not be empty")
        else if (!currentDbContextMigrationsList.contains(it.text))
            error("Migration with such name doesn't exist")
        else null
    }

    fun connectionValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.isEnabled && it.text.isEmpty())
            error("Connection could not be empty")
        else null
    }
}