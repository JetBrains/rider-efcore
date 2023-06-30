package com.jetbrains.observables.ui.dsl

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.observables.Observable
import com.jetbrains.observables.ObservableProperty
import com.jetbrains.rider.plugins.efcore.ui.IconComboBoxRendererAdapter
import com.jetbrains.rider.plugins.efcore.ui.items.IconItem
import java.awt.event.ItemEvent
import javax.swing.ComboBoxEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor

fun <T : IconItem<*>> Row.iconComboBox(
        selectedItemProperty: com.jetbrains.observables.Observable<T?>,
        availableItemsProperty: com.jetbrains.observables.Observable<List<T?>>
): Cell<ComboBox<T>> {
    val model = DefaultComboBoxModel<T>()
        .apply {
            selectedItemProperty.afterChange {
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

    return comboBox(model, IconComboBoxRendererAdapter())
        .bindItem(selectedItemProperty.getter, selectedItemProperty.setter)
        .align(AlignX.FILL)
        .applyToComponent {
            // Setter provided above called only on submit, so we need additional change detection
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    selectedItemProperty.setter(item)
                }
            }
        }
}

fun <T : IconItem<TValue>, TValue> Row.editableComboBox(
        selectedTextProperty: com.jetbrains.observables.Observable<String>,
        availableItemsProperty: com.jetbrains.observables.Observable<List<T>>,
        itemMapper: (TValue) -> String
): Cell<ComboBox<T>> {
    val model = DefaultComboBoxModel<T>()
        .apply {
            availableItemsProperty.afterChange {
                removeAllElements()
                addAll(it)
            }
        }

    return comboBox(model, IconComboBoxRendererAdapter())
        .applyToComponent {
            isEditable = true
            editor = object : BasicComboBoxEditor() {
                override fun setItem(anObject: Any?) {
                    val item = anObject as? IconItem<TValue>
                    if (item == null && anObject is String?)
                        editor.text = anObject
                    else if (item != null)
                        editor.text = itemMapper(item.data)
                }

                override fun getItem(): Any {
                    return editor.text
                }
            }

            val editorComponent = editor.editorComponent as JTextField

            editorComponent.whenTextChanged {
                selectedTextProperty.value = editorComponent.text
            }

            selectedTextProperty.afterChange {
                editorComponent.text = it
            }
        }
        .align(AlignX.FILL)
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

private fun Row.textFieldWithCompletion(
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
