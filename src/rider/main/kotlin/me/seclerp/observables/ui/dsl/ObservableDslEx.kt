package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.textCompletion.TextFieldWithCompletion
import me.seclerp.observables.Observable
import me.seclerp.observables.ObservableProperty
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon

fun <T : IconItem<*>> Row.iconComboBox(
    selectedItemProperty: Observable<T?>,
    availableItemsProperty: Observable<List<T?>>
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
        .horizontalAlign(HorizontalAlign.FILL)
        .applyToComponent {
            // Setter provided above called only on submit, so we need additional change detection
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    selectedItemProperty.setter(item)
                }
            }
        }
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
