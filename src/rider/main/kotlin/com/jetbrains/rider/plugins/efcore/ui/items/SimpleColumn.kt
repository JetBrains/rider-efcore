package com.jetbrains.rider.plugins.efcore.ui.items

import com.intellij.util.ui.ColumnInfo
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle

class SimpleColumn : ColumnInfo<SimpleItem, String>(EfCoreUiBundle.message("simple.column.name")) {
    override fun isCellEditable(item: SimpleItem?): Boolean {
        return true
    }

    override fun setValue(item: SimpleItem?, value: String?) {
        item?.data = value ?: ""
    }

    override fun valueOf(item: SimpleItem?): String? = item?.data
}