package com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.observables.ObservableCollection
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.shared.validateRelativeFolderPath
import com.jetbrains.rider.plugins.efcore.ui.items.DbConnectionItem
import com.jetbrains.rider.plugins.efcore.ui.items.DbProviderItem
import com.jetbrains.rider.plugins.efcore.ui.items.SimpleItem
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.JTextComponent

class ScaffoldDbContextValidator(
    private val migrationsProjectFolderGetter: () -> String
)  {
    fun connectionValidation(): ValidationInfoBuilder.(ComboBox<DbConnectionItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.editor.editorComponent as JTextComponent).text.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.connection.could.not.be.empty"))
        else null
    }

    fun providerValidation(): ValidationInfoBuilder.(ComboBox<DbProviderItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.editor.editorComponent as JTextComponent).text.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.provider.could.not.be.empty"))
        else
            null
    }

    fun outputFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (migrationsProjectFolderGetter().isEmpty())
            null
        else
            validateRelativeFolderPath(it.text.trim(), migrationsProjectFolderGetter()).let {
                if (it.isValid) null else error(it.errorMessage!!)
            }
    }

    fun dbContextNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.class.name.could.not.be.empty"))
        else
            null
    }

    fun dbContextFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.folder.should.not.be.empty"))
        else
            null
    }

    fun tableSchemaValidation(
        tablesList: ObservableCollection<SimpleItem>,
        scaffoldAllTables: ComponentPredicate
    ): ValidationInfoBuilder.(JPanel) -> ValidationInfo? = {
        if (!scaffoldAllTables.invoke() && tablesList.none { it.data.isNotEmpty() })
            error(EfCoreUiBundle.message("dialog.message.tables.schemas.should.not.be.empty"))
        else
            null
    }
}