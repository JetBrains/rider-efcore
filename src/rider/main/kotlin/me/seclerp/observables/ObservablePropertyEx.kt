package me.seclerp.observables

/**
 * Creates new [ObservableProperty] and binds to its value change using [mapping] as transformation.
 * When new value is not null, [mapping] is used to transform value.
 * When new value is null, null is assigned.
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.map(mapping: (T) -> T2): ObservableProperty<T2> {
    val currentValue = if (value == null) null else mapping(notNullValue)
    val newProperty = ObservableProperty(currentValue)
    afterChange {
        val value = getter()
        if (value == null) newProperty.value = null
        else newProperty.value = mapping(value)
    }

    return newProperty
}

/**
 * Creates new [ObservableProperty] and binds to its value change using [mapping] as transformation.
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.mapNullable(mapping: (T?) -> T2?): ObservableProperty<T2> {
    val newProperty = ObservableProperty(mapping(value))
    afterChange { newProperty.value = mapping(getter()) }

    return newProperty
}

/**
 * @param warmUp if true, additionally to creating mapping, value will be set immediately
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.bind(warmUp: Boolean, bindTo: ObservableProperty<T2>, mappingTo: (T2) -> T) {
    bindTo.afterChange(warmUp) {
        if (it == null) value = null
        else value = mappingTo(it)
    }
}

fun <T2 : Any, T : Any> ObservableProperty<T>.bind(bindTo: ObservableProperty<T2>, mappingTo: (T2) -> T) {
    bind(warmUp = false, bindTo, mappingTo)
}

/**
 * @param warmUp if true, additionally to creating mapping, value will be set immediately
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.bind(warmUp: Boolean, bindTo: ObservableProperty<T2>, mappingTo: (T2) -> T, mappingFrom: (T) -> T2) {
    bind(warmUp, bindTo, mappingTo)
    bindTo.bind(this, mappingFrom)
}

fun <T2 : Any, T : Any> ObservableProperty<T>.bind(bindTo: ObservableProperty<T2>, mappingTo: (T2) -> T, mappingFrom: (T) -> T2) {
    bind(warmUp = false, bindTo, mappingTo, mappingFrom)
}

/**
 * @param warmUp if true, additionally to creating mapping, value will be set immediately
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.bindNullable(warmUp: Boolean, bindTo: ObservableProperty<T2>, mappingTo: (T2?) -> T?) {
    bindTo.afterChange(warmUp) { value = mappingTo(it) }
}

fun <T2 : Any, T : Any> ObservableProperty<T>.bindNullable(bindTo: ObservableProperty<T2>, mappingTo: (T2?) -> T?) {
    bindNullable(warmUp = false, bindTo, mappingTo)
}

/**
 * @param warmUp if true, additionally to creating mapping, value will be set immediately
 */
fun <T2 : Any, T : Any> ObservableProperty<T>.bindNullable(warmUp: Boolean, bindTo: ObservableProperty<T2>, mappingTo: (T2?) -> T?, mappingFrom: (T?) -> T2?) {
    bindNullable(warmUp, bindTo, mappingTo)
    bindTo.bindNullable(this, mappingFrom)
}

fun <T2 : Any, T : Any> ObservableProperty<T>.bindNullable(bindTo: ObservableProperty<T2>, mappingTo: (T2?) -> T?, mappingFrom: (T?) -> T2?) {
    bindNullable(warmUp = false, bindTo, mappingTo, mappingFrom)
}