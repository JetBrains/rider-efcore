package com.jetbrains.rider.plugins.efcore.observables

internal fun <T> observableList() = ObservableCollection<T>()

internal fun <T, T2> ObservableCollection<T>.bindElement(bindTo: ObservableCollection<T2>, mappingTo: (T) -> T2) {
    afterAdded { bindTo.add(mappingTo(it)) }
    afterRemoved { bindTo.remove(mappingTo(it)) }
}

internal fun <T, T2> ObservableCollection<T>.bindElement(bindTo: ObservableCollection<T2>, mappingTo: (T) -> T2, mappingFrom: (T2) -> T) {
    this.bindElement(bindTo, mappingTo)
    bindTo.bindElement(this, mappingFrom)
}