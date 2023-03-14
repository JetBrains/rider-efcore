package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import me.seclerp.rider.plugins.efcore.ui.items.DbConnectionItem
import javax.swing.JTextField
import javax.swing.text.JTextComponent

class ScaffoldDbContextValidator {
    fun connectionValidation(): ValidationInfoBuilder.(ComboBox<DbConnectionItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.editor.editorComponent as JTextComponent).text.isEmpty())
            error("Connection could not be empty")
        else null
    }

    fun providerValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Provider could not be empty")
        else
            null
    }

    fun outputFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("Output folder could not be empty")
        else
            null
    }

    fun dbContextNameValidation(): ValidationInfoBuilder.(JTextField) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("DbContext class name could not be empty")
        else
            null
    }

    fun dbContextFolderValidation(): ValidationInfoBuilder.(TextFieldWithBrowseButton) -> ValidationInfo? = {
        if (it.text.trim().isEmpty())
            error("DbContext folder could not be empty")
        else
            null
    }
}