package me.seclerp.rider.plugins.efcore.ui.items

import javax.swing.Icon

open class IconItem<T>(
    val displayName: String,
    val icon: Icon?,
    val data: T
) {
    override fun toString() =
        "${this::class.java.simpleName}(" +
            "displayName=${displayName}" +
            ", icon=${icon}, data=${data}" +
        ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class != this::class) return false

        other as IconItem<*>

        return other.data?.equals(data) ?: false
    }

    override fun hashCode() = data.hashCode()
}
