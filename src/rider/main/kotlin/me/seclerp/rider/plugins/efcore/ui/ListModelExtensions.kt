package me.seclerp.rider.plugins.efcore.ui

import javax.swing.ListModel

fun <T> ListModel<T>.firstOrNull(predicate: (T) -> Boolean): T? {
    var foundItem: T? = null
    for (i in 0 until size) {
        val current = getElementAt(i)
        if (predicate(current)) {
            foundItem = current
        }
    }

    return foundItem
}