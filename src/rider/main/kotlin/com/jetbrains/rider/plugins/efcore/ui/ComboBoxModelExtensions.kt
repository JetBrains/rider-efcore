package com.jetbrains.rider.plugins.efcore.ui

import javax.swing.ComboBoxModel

val <T> ComboBoxModel<T>.elements: List<T> get() {
    return buildList {
        for (i in 0 until getSize()) {
            add(getElementAt(i))
        }
    }
}