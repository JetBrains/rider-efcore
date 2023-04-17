package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.ui.items.DbConnectionItem
import me.seclerp.rider.plugins.efcore.ui.items.DbProviderItem
import javax.swing.JTextField
import javax.swing.text.JTextComponent

class ScaffoldDbContextValidator {
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
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.output.folder.could.not.be.empty"))
        else
            null
    }

    fun dbContextNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.class.name.could.not.be.empty"))
        else
            null
    }

    fun dbContextFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.folder.could.not.be.empty"))
        else
            null
    }
}