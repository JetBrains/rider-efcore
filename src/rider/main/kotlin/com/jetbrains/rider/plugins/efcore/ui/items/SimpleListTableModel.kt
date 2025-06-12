package com.jetbrains.rider.plugins.efcore.ui.items

import com.intellij.util.ui.ListTableModel

class SimpleListTableModel(items: MutableList<SimpleItem>)
    : ListTableModel<SimpleItem>(arrayOf(SimpleColumn()), items)