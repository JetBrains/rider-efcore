package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

class AddMigrationValidator {
    fun migrationNameValidation(currentDbContextMigrationsList: List<String>): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Migration name could not be empty")
        else if (currentDbContextMigrationsList.contains(it.text.trim()))
            error("Migration with such name already exist")
        else
            null
    }
}