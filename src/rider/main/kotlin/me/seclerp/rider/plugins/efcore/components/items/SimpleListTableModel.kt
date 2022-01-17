package me.seclerp.rider.plugins.efcore.components.items

import com.intellij.util.ui.ListTableModel

class SimpleListTableModel(items: MutableList<SimpleItem>)
    : ListTableModel<SimpleItem>(arrayOf(SimpleColumn()), items) {
}