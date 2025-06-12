package com.jetbrains.rider.plugins.efcore.observables

import java.util.Random

internal open class ObservableProperty<T>(defaultValue: T) : Observable<T> {
    private var internalValue = defaultValue

    //
    // Implementation of Observable
    override val id = random.nextInt(1000)

    override var value: T
        get() = getter()
        set(value) { setter(value) }

    override val getter: () -> T = { internalValue }

    override val setter: (T) -> Unit = {
        if (internalValue != it || !isInitialized) {
            internalValue = it
            isInitialized = true
            onChange.invoke(internalValue)
        }
    }

    override val onChange: Event<T> = Event()

    override var isInitialized: Boolean = false
        protected set

    override fun afterChange(effect: (T) -> Unit) {
        onChange += effect
    }

    override fun initialize(value: T)  {
        if (this.value == value) {
            isInitialized = true
            onChange.invoke(value)
            return
        }

        this.value = value
    }

    companion object {
        private val random = Random()
    }
}