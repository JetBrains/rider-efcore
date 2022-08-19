package me.seclerp.observables

open class ObservableProperty<T : Any>(initialValue: T?) {
    private var internalValue = initialValue

    var value: T?
        get() = getter()
        set(value) { setter(value) }

    val notNullValue: T
        get() = value!!

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
}