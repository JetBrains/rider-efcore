package com.jetbrains.rider.plugins.efcore.observables

import com.intellij.openapi.diagnostic.logger
import org.jetbrains.annotations.NonNls

internal fun <TSource> observable(@NonNls defaultValue: TSource): ObservableProperty<TSource> = ObservableProperty(defaultValue)

internal fun <TSource, TDest> Observable<TSource>.bind(bindFrom: Observable<TDest>, @NonNls mappingFrom: (TDest) -> TSource) {
    bindFrom.afterChange {
        value = mappingFrom(it)
    }
}

internal fun <TSource, TDest : TDestSafe?, TDestSafe> Observable<TSource>.bindSafe(bindFrom: Observable<TDest>, @NonNls mappingFrom: (TDestSafe) -> TSource) {
    bindFrom.afterChange {
        if (it != null) {
            value = mappingFrom(it)
        }
    }
}

internal fun <TSource, TDest> Observable<TSource>.bind(bindFrom: Observable<TDest>, @NonNls mappingFrom: (TDest) -> TSource, @NonNls mappingTo: (TSource) -> TDest) {
    bind(bindFrom, mappingFrom)
    bindFrom.bind(this, mappingTo)
}

internal fun <TSource, TDest : TDestSafe?, TDestSafe> Observable<TSource>.bindSafe(bindFrom: Observable<TDest>, @NonNls mappingFrom: (TDestSafe) -> TSource, @NonNls mappingTo: (TSource) -> TDest) {
    bindSafe(bindFrom, mappingFrom)
    bindFrom.bind(this, mappingTo)
}

internal inline fun <reified TValue, reified TObservable : Observable<TValue>> TObservable.withLogger(propertyDebugName: String? = null): TObservable {
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