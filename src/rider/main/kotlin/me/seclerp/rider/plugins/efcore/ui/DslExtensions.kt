@file:Suppress("UnstableApiUsage")

package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.PropertyBinding
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rdclient.util.idea.toIOFile
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.event.ItemEvent
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon
import kotlin.reflect.KMutableProperty0

//
// iconComboBox
//

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    binding: PropertyBinding<T?>
): Cell<ComboBox<T>> =

    comboBox(model, IconComboBoxRendererAdapter())
        .bindItem(binding)
        .horizontalAlign(HorizontalAlign.FILL)
        .applyToComponent {
            // Setter provided above called only on submit, so we need additional change detection
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    binding.set(item)
                }
            }
        }

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    getter: () -> T?,
    setter: (T?) -> Unit
): Cell<ComboBox<T>> {
    return iconComboBox(model, PropertyBinding(getter, setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> {
    return iconComboBox(model, PropertyBinding(property.getter, property.setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    binding: PropertyBinding<T?>
): Cell<ComboBox<T>> {
    return iconComboBox(DefaultComboBoxModel(items), binding)
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    getter: () -> T?,
    setter: (T?) -> Unit
): Cell<ComboBox<T>> {
    return iconComboBox(items, PropertyBinding(getter, setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> {
    return iconComboBox(items, PropertyBinding(property.getter, property.setter))
}

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
    getter: () -> String?,
    project: Project? = null,
    browseDialogTitle: String? = null,
): Cell<TextFieldWithBrowseButton> {

    val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()

    val textFieldWithBrowseButtonCell = textFieldWithBrowseButton(browseDialogTitle, project, fileChooserDescriptor) {
        formatRelativePathToFolder(it, getter())
    }.horizontalAlign(HorizontalAlign.FILL)

    return textFieldWithBrowseButtonCell
}

private fun formatRelativePathToFolder(folderFromTextFieldWithBrowseButton: VirtualFile, pathRelativeTo: String?) : String {
    val pathRelativeToAsFile = File(pathRelativeTo!!).path
    val relativePath = folderFromTextFieldWithBrowseButton.toIOFile().relativeTo(File(pathRelativeToAsFile)).path

    return relativePath
}