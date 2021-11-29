package me.seclerp.rider.plugins.efcore.components

import javax.swing.Icon

data class IconItem<T>(
    val displayName: String,
    val icon: Icon,
    val data: T
)
