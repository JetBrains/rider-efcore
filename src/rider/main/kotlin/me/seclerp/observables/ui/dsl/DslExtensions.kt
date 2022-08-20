package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.PropertyBinding
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.rdclient.util.idea.toIOFile
import me.seclerp.observables.Observable
import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.ObservableProperty
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
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
    selectedItemProperty: Observable<T?>,
    availableItemsProperty: Observable<List<T?>>
): Cell<ComboBox<T>> {
    val model = DefaultComboBoxModel<T>()
        .apply {
            selectedItemProperty.afterChange {
                // We are going to introduce a little hack here. In some cases, selected item comes BEFORE new
                // available items are set to the model. In such case, we will add selected item to the model. After
                // actual data for available items will be presented, old item will be removed.
                if (getIndexOf(it) == -1) {
                    addElement(it)
                }

                if (selectedItem != it)
                    selectedItem = it
            }
            availableItemsProperty.afterChange {
                val selectedItem = this.selectedItem
                removeAllElements()
                addAll(it)
                this.selectedItem = selectedItem
            }
        }

    return iconComboBox(model, selectedItemProperty.getter, selectedItemProperty.setter)
}

@Deprecated("Use ObservableProperty-based overload instead.", level = DeprecationLevel.WARNING)
fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> = iconComboBox(model, property.getter, property.setter)

@Deprecated("Use ObservableProperty-based overload instead.", level = DeprecationLevel.WARNING)
fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    property: KMutableProperty0<T?>
): Cell<ComboBox<T>> = iconComboBox(DefaultComboBoxModel(items), property.getter, property.setter)

//
// textFieldWithCompletion
//

fun Row.textFieldWithCompletion(
    getter: () -> String,
    setter: (String) -> Unit,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    val provider = TextFieldWithAutoCompletion.StringsCompletionProvider(completions, icon)
    val textField = TextFieldWithCompletion(project, provider, getter(), true, true, false, false)
    textField.addDocumentListener(object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            setter(event.document.text)
        }
    })

    return cell(textField)
}

fun Row.textFieldWithCompletion(
    property: ObservableProperty<String>,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    return textFieldWithCompletion(property.getter, property.setter, completions, project, icon)
        .applyToComponent { property.afterChange { this.text = it } }
}

@Deprecated("Use ObservableProperty-based overload instead.", level = DeprecationLevel.WARNING)
fun Row.textFieldWithCompletion(
    binding: PropertyBinding<String>,
    completions: MutableList<String>,
    project: Project? = null,
    icon: Icon? = null
): Cell<TextFieldWithCompletion> {
    return textFieldWithCompletion(binding.get, binding.set, completions, project, icon)
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

fun Cell<JBCheckBox>.bindSelected(
    property: ObservableProperty<Boolean>,
): Cell<JBCheckBox> {
    return this
        .bindSelected(property.getter, property.setter)
        .applyToComponent { property.afterChange { this.isSelected = it } }
}

fun Cell<JBTextField>.bindText(
    property: ObservableProperty<String>,
): Cell<JBTextField> {
    return this
        .bindText(property.getter, property.setter)
        .applyToComponent { property.afterChange { this.text = it } }
}

@JvmName("bindTextTextFieldWithBrowseButton")
fun Cell<TextFieldWithBrowseButton>.bindText(
    property: ObservableProperty<String>,
): Cell<TextFieldWithBrowseButton> {
    return this
        .bindText(property.getter, property.setter)
        .applyToComponent { property.afterChange { this.text = it } }
}


fun <T : JComponent> Cell<T>.monospaced(): Cell<T> = applyToComponent {
    monospaced()
}

fun JComponent.monospaced(): JComponent = apply {
    font = EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN)
}