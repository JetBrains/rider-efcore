package me.seclerp.rider.plugins.efcore.components.items

import javax.swing.Icon

open class IconItem<T>(
    val displayName: String,
    val icon: Icon,
    val data: T
)
