package me.seclerp.rider.plugins.efcore.ui.items

import com.intellij.util.ui.ColumnInfo

class SimpleColumn : ColumnInfo<SimpleItem, String>("Table Name") {
    override fun isCellEditable(item: SimpleItem?): Boolean {
        return true
    }

    override fun setValue(item: SimpleItem?, value: String?) {
        item?.data = value ?: ""
    }

    override fun valueOf(item: SimpleItem?): String? = item?.data
}