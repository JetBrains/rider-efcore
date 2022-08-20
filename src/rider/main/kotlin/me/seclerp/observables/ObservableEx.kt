package me.seclerp.observables

import com.intellij.openapi.diagnostic.logger

fun <T> observable(defaultValue: T): ObservableProperty<T> = ObservableProperty(defaultValue)

fun <T, T2> Observable<T>.bind(bindTo: Observable<T2>, mappingTo: (T2) -> T) {
    bindTo.afterChange {
        value = mappingTo(it)
    }
}

fun <T, T2> Observable<T>.bind(bindTo: Observable<T2>, mappingTo: (T2) -> T, mappingFrom: (T) -> T2) {
    bind(bindTo, mappingTo)
    bindTo.bind(this, mappingFrom)
}

inline fun <reified TValue, reified TObservable : Observable<TValue>> TObservable.withLogger(propertyDebugName: String? = null): TObservable {
    val propertyType = TValue::class.java.simpleName
    val logger = logger<TObservable>()
    val propertyId = propertyDebugName ?: id

    afterChange {
        logger.info("Value change notification:\n" +
            "\tProperty: ${propertyId}:${propertyType}\n" +
            "\tWas changed to '${it?.toString()}'\n" +
            "\tObservers notified: ${this.onChange.observers.size}")
    }

    return this
}

fun <T, T2> Observable<T>.compose(with: Observable<T2>): Observable<Pair<T, T2>> =
    observable(Pair(value, with.value))
        .apply {
            bind(this@compose) {
                Pair(it, value.second)
            }
            bind(with) {
                Pair(value.first, it)
            }
        }