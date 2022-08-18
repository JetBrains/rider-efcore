package me.seclerp.rider.plugins.efcore.features.shared.models

import me.seclerp.rider.plugins.efcore.Event

class ReactiveProperty<T : Any>(initialValue: T?) {
    private var internalValue = initialValue

    var value: T?
        get() = getter()
        set(value) { setter(value) }

    val getter: () -> T? = { internalValue }
    val setter: (T?) -> Unit = {
        if (internalValue != it) {
            internalValue = it
            onChange.invoke(value)
        }
    }

    val onChange: Event<T?> = Event()

    /**
     * @param warmUp if true, additionally to creating mapping, value will be set immediately
     */
    fun afterChange(warmUp: Boolean, effect: (T?) -> Unit) {
        if (warmUp) {
            effect(getter())
        }

        onChange += effect
    }

    fun afterChange(effect: (T?) -> Unit) {
        afterChange(false, effect)
    }

    fun <T2 : Any> map(mapping: (T) -> T2): ReactiveProperty<T2> {
        val currentValue = if (value == null) null else mapping(value!!)
        val newProperty = ReactiveProperty(currentValue)
        afterChange {
            val value = getter()
            if (value == null) newProperty.value = null
            else newProperty.value = mapping(value)
        }

        return newProperty
    }

    fun <T2 : Any> mapNullable(mapping: (T?) -> T2?): ReactiveProperty<T2> {
        val newProperty = ReactiveProperty(mapping(value))
        afterChange { newProperty.value = mapping(getter()) }

        return newProperty
    }

    /**
     * @param warmUp if true, additionally to creating mapping, value will be set immediately
     */
    fun <T2 : Any> bind(warmUp: Boolean, bindTo: ReactiveProperty<T2>, mappingTo: (T2) -> T) {
        bindTo.afterChange(warmUp) {
            if (it == null) value = null
            else value = mappingTo(it)
        }
    }

    fun <T2 : Any> bind(bindTo: ReactiveProperty<T2>, mappingTo: (T2) -> T) {
        bind(warmUp = false, bindTo, mappingTo)
    }

    /**
     * @param warmUp if true, additionally to creating mapping, value will be set immediately
     */
    fun <T2 : Any> bind(warmUp: Boolean, bindTo: ReactiveProperty<T2>, mappingTo: (T2) -> T, mappingFrom: (T) -> T2) {
        bind(warmUp, bindTo, mappingTo)
        bindTo.bind(this, mappingFrom)
    }

    fun <T2 : Any> bind(bindTo: ReactiveProperty<T2>, mappingTo: (T2) -> T, mappingFrom: (T) -> T2) {
        bind(warmUp = false, bindTo, mappingTo, mappingFrom)
    }

    /**
     * @param warmUp if true, additionally to creating mapping, value will be set immediately
     */
    fun <T2 : Any> bindNullable(warmUp: Boolean, bindTo: ReactiveProperty<T2>, mappingTo: (T2?) -> T?) {
        bindTo.afterChange(warmUp) { value = mappingTo(it) }
    }

    fun <T2 : Any> bindNullable(bindTo: ReactiveProperty<T2>, mappingTo: (T2?) -> T?) {
        bindNullable(warmUp = false, bindTo, mappingTo)
    }

    /**
     * @param warmUp if true, additionally to creating mapping, value will be set immediately
     */
    fun <T2 : Any> bindNullable(warmUp: Boolean, bindTo: ReactiveProperty<T2>, mappingTo: (T2?) -> T?, mappingFrom: (T?) -> T2?) {
        bindNullable(warmUp, bindTo, mappingTo)
        bindTo.bindNullable(this, mappingFrom)
    }

    fun <T2 : Any> bindNullable(bindTo: ReactiveProperty<T2>, mappingTo: (T2?) -> T?, mappingFrom: (T?) -> T2?) {
        bindNullable(warmUp = false, bindTo, mappingTo, mappingFrom)
    }

    operator fun timesAssign(newValue: T) {
        value = newValue
    }
}