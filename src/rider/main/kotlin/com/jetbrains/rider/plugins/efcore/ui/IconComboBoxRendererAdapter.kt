package com.jetbrains.rider.plugins.efcore.ui

import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.ui.items.IconItem
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

        when (value) {
            null -> {
                delegatingRenderer.text = EfCoreUiBundle.message("none")
            }
            else -> {
                delegatingRenderer.text = value.displayName
                delegatingRenderer.icon = value.icon
            }
        }

        return delegatingRenderer
    }
}