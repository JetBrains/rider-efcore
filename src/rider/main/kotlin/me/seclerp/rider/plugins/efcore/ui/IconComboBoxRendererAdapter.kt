package me.seclerp.rider.plugins.efcore.ui

import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.ui.items.IconItem
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.ListCellRenderer

class IconComboBoxRendererAdapter<T : IconItem<*>>: ListCellRenderer<T?> {
    private val delegatingRenderer = DefaultListCellRenderer()

    override fun getListCellRendererComponent(
        list: JList<out T>?,
        value: T?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        delegatingRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        if (value != null) {
            delegatingRenderer.text = value.displayName
            delegatingRenderer.icon = value.icon
        } else {
            delegatingRenderer.text = EfCoreUiBundle.message("none")
        }

        return delegatingRenderer
    }
}