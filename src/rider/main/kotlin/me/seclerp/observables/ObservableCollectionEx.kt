package me.seclerp.observables

fun <T2 : Any, T : Any> ObservableCollection<T>.bind(bindTo: ObservableCollection<T2>, mappingTo: (T?) -> T2?) {
    this.afterAdded { bindTo.add(mappingTo(it)) }
}

fun <T2 : Any, T : Any> ObservableCollection<T>.bind(bindTo: ObservableCollection<T2>, mappingTo: (T?) -> T2?, mappingFrom: (T2?) -> T?) {
    this.bind(bindTo, mappingTo)
    bindTo.bind(this, mappingFrom)
}