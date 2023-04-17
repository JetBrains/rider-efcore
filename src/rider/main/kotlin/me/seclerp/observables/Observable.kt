package me.seclerp.observables

import org.jetbrains.annotations.NonNls

interface Observable<T> {
    val id: Int
    var value: T
    val onChange: Event<T>
    val isInitialized: Boolean
    val getter: () -> T
    val setter: (T) -> Unit

    fun afterChange(@NonNls effect: (T) -> Unit)

    /**
     * Set the observable value to [value] and notifies subscribers even if value wasn't changed.
     */
    fun initialize(value: T)
}