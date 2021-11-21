package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.LayoutBuilder
import java.awt.event.ItemEvent

@Suppress("UNCHECKED_CAST")
fun <T> LayoutBuilder.comboBox(values: Array<T>, selectedValue: T? = null, handleSelected: (T) -> Unit): ComboBox<T> {
    val comboBox = ComboBox(values)
    val selectedIndex = values.indexOf(selectedValue)
    if (selectedIndex == -1) {
        // TODO
        throw Exception()
    }
    comboBox.selectedIndex = selectedIndex
    comboBox.addItemListener {
        if (it.stateChange == ItemEvent.SELECTED) {
            handleSelected(it.item as T)
        }
    }

    return comboBox
}