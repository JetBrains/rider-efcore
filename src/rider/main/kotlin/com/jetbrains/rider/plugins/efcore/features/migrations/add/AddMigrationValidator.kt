package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import javax.swing.JTextField

class AddMigrationValidator(
    private val dataCtx: AddMigrationDataContext
) {
    fun migrationNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migration.name.could.not.be.empty"))
        else if (dataCtx.availableMigrations.value.any { migration -> migration.migrationLongName == it.text.trim() })
            error(EfCoreUiBundle.message("dialog.message.migration.with.such.name.already.exist"))
        else
            null
    }

    fun migrationsOutputFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migrations.output.folder.could.not.be.empty"))
        else
            null
    }
}