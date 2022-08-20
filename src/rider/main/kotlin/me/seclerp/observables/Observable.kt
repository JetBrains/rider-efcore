package me.seclerp.observables

interface Observable<T> {
    val id: Int
    var value: T
    val onChange: Event<T>
    val isInitialized: Boolean
    val getter: () -> T
    val setter: (T) -> Unit

    fun afterChange(effect: (T) -> Unit)

    /**
     * Set the observable value to [value] and notifies subscribers even if value wasn't changed.
     */
    fun initialize(value: T)
}