package com.jetbrains.rider.plugins.efcore.features.database.update

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.ui.items.DbConnectionItem
import com.jetbrains.rider.plugins.efcore.ui.items.MigrationItem
import javax.swing.text.JTextComponent

class UpdateDatabaseValidator(
    private val currentDbContextMigrationsList: MutableList<MigrationItem?>
) {
    fun targetMigrationValidation(): ValidationInfoBuilder.(ComboBox<MigrationItem>) -> ValidationInfo? = {
        if (it.selectedItem == null)
            error(EfCoreUiBundle.message("dialog.message.target.migration.should.be.specified"))
        else
            null
    }

    fun connectionValidation(): ValidationInfoBuilder.(ComboBox<DbConnectionItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.editor.editorComponent as JTextComponent).text.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.connection.could.not.be.empty"))
        else null
    }
}