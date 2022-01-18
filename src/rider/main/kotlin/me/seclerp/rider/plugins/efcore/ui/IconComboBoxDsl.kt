package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Cell
import com.intellij.ui.layout.CellBuilder
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel

fun <T : IconItem<*>> Cell.iconComboBox(
    model: DefaultComboBoxModel<T>,
    getter: () -> T?,
    setter: (T?) -> Unit): CellBuilder<ComboBox<T>> {

    val comboBoxBuilder =
        comboBox(model,
            getter,
            setter,
            IconComboBoxRendererAdapter()
        ).constraints(CCFlags.pushX, CCFlags.growX)

    // Setter provided above called only on submit, so we need additional change detection
    comboBoxBuilder.component.addItemListener {
        if (it.stateChange == ItemEvent.SELECTED) {
            setter(it.item as T?)
        }
    }

    return comboBoxBuilder
}