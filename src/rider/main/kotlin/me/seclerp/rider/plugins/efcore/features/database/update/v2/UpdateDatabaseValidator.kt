package me.seclerp.rider.plugins.efcore.features.database.update.v2

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.textCompletion.TextFieldWithCompletion

class UpdateDatabaseValidator(
    private val currentDbContextMigrationsList: MutableList<String>
) {
    fun targetMigrationValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Target migration could not be empty")
        else if (!currentDbContextMigrationsList.contains(it.text))
            error("Migration with such name doesn't exist")
        else null
    }

    fun connectionValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.text.isEmpty())
            error("Connection could not be empty")
        else null
    }
}