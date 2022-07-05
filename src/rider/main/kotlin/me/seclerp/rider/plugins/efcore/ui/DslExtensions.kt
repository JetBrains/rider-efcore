@file:Suppress("UnstableApiUsage")

package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RelativeFont
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.PropertyBinding
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rdclient.util.idea.toIOFile
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.Font
import java.awt.event.ItemEvent
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

//
// iconComboBox
//

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    getter: () -> T?,
    setter: (T?) -> Unit
): Cell<ComboBox<T>> =
    comboBox(model, IconComboBoxRendererAdapter())
        .bindItem(getter, setter)
        .horizontalAlign(HorizontalAlign.FILL)
        .applyToComponent {
            // Setter provided above called only on submit, so we need additional change detection
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    setter(item)
                }
            }
        }


fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> = iconComboBox(model, property.getter, property.setter)

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    getter: () -> T?,
    setter: (T?) -> Unit
): Cell<ComboBox<T>> = iconComboBox(DefaultComboBoxModel(items), getter, setter)

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> = iconComboBox(DefaultComboBoxModel(items), property.getter, property.setter)

//
// textFieldWithCompletion
//

fun Row.textFieldWithCompletion(
    binding: PropertyBinding<String>,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    val provider = TextFieldWithAutoCompletion.StringsCompletionProvider(completions, icon)
    val textField = TextFieldWithCompletion(project, provider, binding.get(), true, true, false, false)
    textField.editor
    textField.addDocumentListener(object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            binding.set(event.document.text)
        }
    })

    return cell(textField)
}

fun Row.textFieldWithCompletion(
    getter: () -> String,
    setter: (String) -> Unit,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    return textFieldWithCompletion(PropertyBinding(getter, setter), completions, project, icon)
}

fun Row.textFieldWithCompletion(
    property: KMutableProperty0<String>,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    return textFieldWithCompletion(PropertyBinding(property.getter, property.setter), completions, project, icon)
}

fun Row.textFieldForRelativeFolder(
    basePathGetter: () -> String,
    project: Project? = null,
    browseDialogTitle: String? = null,
): Cell<TextFieldWithBrowseButton> {

    val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    val textFieldWithBrowseButtonCell = textFieldWithBrowseButton(browseDialogTitle, project, fileChooserDescriptor) {
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
    getter: () -> String,
    setter: (String) -> Unit
): Cell<ExpandableTextField> {
    return simpleExpandableTextFieldBase()
        .bindText(getter, setter)
}

fun Row.simpleExpandableTextField(
    property: KMutableProperty0<String>,
): Cell<ExpandableTextField> {
    return simpleExpandableTextFieldBase()
        .bindText(property)
}

private fun Row.simpleExpandableTextFieldBase(): Cell<ExpandableTextField> =
    expandableTextField({ mutableListOf(it) }, { it[0] } )
        .applyToComponent {
            caretPosition = 0
        }
        .monospaced()


fun <T : JComponent> Cell<T>.monospaced(): Cell<T> = applyToComponent {
    monospaced()
}

fun JComponent.monospaced(): JComponent = apply {
    font = EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN)
}