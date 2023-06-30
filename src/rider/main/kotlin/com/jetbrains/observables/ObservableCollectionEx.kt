package com.jetbrains.observables

fun <T> observableList() = com.jetbrains.observables.ObservableCollection<T>()

fun <T, T2> com.jetbrains.observables.ObservableCollection<T>.bindElement(bindTo: com.jetbrains.observables.ObservableCollection<T2>, mappingTo: (T) -> T2) {
    afterAdded { bindTo.add(mappingTo(it)) }
    afterRemoved { bindTo.remove(mappingTo(it)) }
}

fun <T, T2> com.jetbrains.observables.ObservableCollection<T>.bindElement(bindTo: com.jetbrains.observables.ObservableCollection<T2>, mappingTo: (T) -> T2, mappingFrom: (T2) -> T) {
    this.bindElement(bindTo, mappingTo)
    bindTo.bindElement(this, mappingFrom)
}