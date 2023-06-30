package com.jetbrains.observables

import com.intellij.openapi.diagnostic.logger
import org.jetbrains.annotations.NonNls

fun <TSource> observable(@NonNls defaultValue: TSource): ObservableProperty<TSource> = ObservableProperty(defaultValue)

fun <TSource, TDest> com.jetbrains.observables.Observable<TSource>.bind(bindFrom: com.jetbrains.observables.Observable<TDest>, @NonNls mappingFrom: (TDest) -> TSource) {
    bindFrom.afterChange {
        value = mappingFrom(it)
    }
}

fun <TSource, TDest : TDestSafe?, TDestSafe> com.jetbrains.observables.Observable<TSource>.bindSafe(bindFrom: com.jetbrains.observables.Observable<TDest>, @NonNls mappingFrom: (TDestSafe) -> TSource) {
    bindFrom.afterChange {
        if (it != null) {
            value = mappingFrom(it)
        }
    }
}

fun <TSource, TDest> com.jetbrains.observables.Observable<TSource>.bind(bindFrom: com.jetbrains.observables.Observable<TDest>, @NonNls mappingFrom: (TDest) -> TSource, @NonNls mappingTo: (TSource) -> TDest) {
    bind(bindFrom, mappingFrom)
    bindFrom.bind(this, mappingTo)
}

fun <TSource, TDest : TDestSafe?, TDestSafe> com.jetbrains.observables.Observable<TSource>.bindSafe(bindFrom: com.jetbrains.observables.Observable<TDest>, @NonNls mappingFrom: (TDestSafe) -> TSource, @NonNls mappingTo: (TSource) -> TDest) {
    bindSafe(bindFrom, mappingFrom)
    bindFrom.bind(this, mappingTo)
}

inline fun <reified TValue, reified TObservable : com.jetbrains.observables.Observable<TValue>> TObservable.withLogger(propertyDebugName: String? = null): TObservable {
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