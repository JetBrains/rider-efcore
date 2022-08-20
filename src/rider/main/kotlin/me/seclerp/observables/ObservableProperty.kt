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

    fun afterChange(effect: (T?) -> Unit) {
        onChange += effect
    }
}