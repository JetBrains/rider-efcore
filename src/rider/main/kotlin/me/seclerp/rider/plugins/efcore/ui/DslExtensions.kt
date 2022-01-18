@file:Suppress("UnstableApiUsage")

package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.PropertyBinding
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel
import kotlin.reflect.KMutableProperty0

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    binding: PropertyBinding<T?>): Cell<ComboBox<T>> =

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
    setter: (T?) -> Unit): Cell<ComboBox<T>> {

    return iconComboBox(model, PropertyBinding(getter, setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    model: DefaultComboBoxModel<T>,
    property: KMutableProperty0<T?>): Cell<ComboBox<T>> {

    return iconComboBox(model, PropertyBinding(property.getter, property.setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    binding: PropertyBinding<T?>): Cell<ComboBox<T>> {

    return iconComboBox(DefaultComboBoxModel(items), binding)
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    getter: () -> T?,
    setter: (T?) -> Unit): Cell<ComboBox<T>> {

    return iconComboBox(items, PropertyBinding(getter, setter))
}

fun <T : IconItem<*>> Row.iconComboBox(
    items: Array<T>,
    property: KMutableProperty0<T?>): Cell<ComboBox<T>> {

    return iconComboBox(items, PropertyBinding(property.getter, property.setter))
}