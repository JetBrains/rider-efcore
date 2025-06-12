package com.jetbrains.rider.plugins.efcore.ui

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindText
import com.jetbrains.rider.plugins.efcore.observables.ObservableProperty
import com.jetbrains.rdclient.util.idea.toIOFile
import java.io.File
import javax.swing.JComponent

fun <T : JComponent> Cell<T>.monospaced(): Cell<T> =
    applyToComponent {
        monospaced()
    }

fun JComponent.monospaced(): JComponent =
    apply {
        font = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
    }

fun Row.textFieldForRelativeFolder(
  basePathGetter: () -> String,
  project: Project? = null,
  browseDialogTitle: @NlsContexts.DialogTitle String? = null,
): Cell<TextFieldWithBrowseButton> {
    val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(browseDialogTitle)
    val textFieldWithBrowseButtonCell = textFieldWithBrowseButton(fileChooserDescriptor, project) {
        val pathRelativeToAsFile = File(basePathGetter()).path
        it.toIOFile().relativeTo(File(pathRelativeToAsFile)).path
    }

    return textFieldWithBrowseButtonCell
}

fun Row.readonlyExpandableTextField(
    getter: () -> String
): Cell<ExpandableTextField> =
    simpleExpandableTextField(getter, {})
        .applyToComponent {
            isEditable = false
        }

fun Row.simpleExpandableTextField(
    property: ObservableProperty<String>,
): Cell<ExpandableTextField> {
    return simpleExpandableTextFieldBase()
        .bindText(property.getter, property.setter)
        .applyToComponent { property.afterChange { this.text = it } }
}

fun Row.simpleExpandableTextField(
    getter: () -> String,
    setter: (String) -> Unit
): Cell<ExpandableTextField> {
    return simpleExpandableTextFieldBase()
        .bindText(getter, setter)
}

private fun Row.simpleExpandableTextFieldBase(): Cell<ExpandableTextField> =
    expandableTextField({ mutableListOf(it) }, { it[0] } )
        .applyToComponent {
            caretPosition = 0
        }
        .monospaced()
