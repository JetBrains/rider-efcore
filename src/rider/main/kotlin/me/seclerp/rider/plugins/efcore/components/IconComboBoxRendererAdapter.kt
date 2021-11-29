package me.seclerp.rider.plugins.efcore.components

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.ListCellRenderer

class IconComboBoxRendererAdapter<T>: ListCellRenderer<IconItem<T>?> {
    private val delegatingRenderer = DefaultListCellRenderer()

    override fun getListCellRendererComponent(
        list: JList<out IconItem<T>>?,
        value: IconItem<T>?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        delegatingRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        delegatingRenderer.text = value!!.displayName
        delegatingRenderer.icon = value.icon

        return delegatingRenderer
    }
}