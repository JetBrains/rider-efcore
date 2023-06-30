package com.jetbrains.observables

class Event<T> {
    private val observersList = mutableListOf<(T) -> Unit>()
    val observers: List<(T) -> Unit> get() = observersList

    operator fun plusAssign(observer: (T) -> Unit) {
        observersList.add(observer)
    }

    operator fun minusAssign(observer: (T) -> Unit) {
        observersList.remove(observer)
    }

    operator fun invoke(value: T) {
        for (observer in observersList)
            observer(value)
    }
}