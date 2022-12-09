package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.textCompletion.TextFieldWithCompletion
import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem

class UpdateDatabaseValidator(
    private val currentDbContextMigrationsList: MutableList<MigrationItem?>
) {
    fun targetMigrationValidation(): ValidationInfoBuilder.(ComboBox<MigrationItem>) -> ValidationInfo? = {
        if (it.selectedItem == null)
            error("Target migration should be specified")
        else
            null
    }

    fun connectionValidation(): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
        if (it.isEnabled && it.text.isEmpty())
            error("Connection could not be empty")
        else null
    }
}