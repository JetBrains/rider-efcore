package com.jetbrains.rider.plugins.efcore.ui

import com.jetbrains.observables.ObservableProperty
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AnyInputDocumentListener(private val inputReceivedProperty: ObservableProperty<Boolean>) : DocumentListener {
    override fun changedUpdate(e: DocumentEvent?) {
        inputReceivedProperty.value = true
    }

    override fun insertUpdate(e: DocumentEvent?) {
        inputReceivedProperty.value = true
    }

    override fun removeUpdate(e: DocumentEvent?) {
        inputReceivedProperty.value = true
    }
}