package me.seclerp.rider.plugins.efcore.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Cell
import com.intellij.ui.layout.CellBuilder
import java.awt.event.ItemEvent
import javax.swing.ComboBoxModel

fun <T> Cell.iconComboBox(
    model: ComboBoxModel<IconItem<T>>,
    getter: () -> IconItem<T>?,
    setter: (IconItem<T>?) -> Unit): CellBuilder<ComboBox<IconItem<T>>> {

    val comboBoxBuilder =
        comboBox(model,
            getter,
            setter,
            IconComboBoxRendererAdapter())
            .constraints(CCFlags.pushX, CCFlags.growX)

    // Setter provided above called only on submit, so we need additional change detection
    comboBoxBuilder.component.addItemListener {
        if (it.stateChange == ItemEvent.SELECTED) {
            setter(it.item as IconItem<T>)
        }
    }

    return comboBoxBuilder
}