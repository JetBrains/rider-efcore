package me.seclerp.observables

import java.util.*

open class ObservableCollection<T>(initialCollection: List<T> = listOf()) : ObservableProperty<List<T>>(initialCollection), MutableList<T> {
    private var items = initialCollection.toMutableList()

    val onAdded: Event<T> = Event()
    val onRemoved: Event<T> = Event()

    fun afterAdded(effect: (T) -> Unit) {
        onAdded += effect
    }

    fun afterRemoved(effect: (T) -> Unit) {
        onRemoved += effect
    }

    override var value: List<T>
        get() = items
        set(value) {
            if (items != value || !isInitialized) {
                items = value.toMutableList()
                isInitialized = true
                onChange.invoke(value)
            }
        }

    //
    // Implementation of AbstractMutableList
    override fun add(element: T): Boolean {
        if (!items.contains(element)) {
            if (items.add(element)) {
                onAdded.invoke(element)
                return true
            }
        }

        return false
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var changed = false
        elements.forEach {
            if (this.add(it)) {
                changed = true
            }
        }

        return changed
    }

    override fun remove(element: T): Boolean {
        var removed = false
        if (items.contains(element)) {
            if (items.remove(element)) {
                removed = true
                onRemoved.invoke(element)
            }
        }

        return removed
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var removed = false
        elements.forEach {
            if (this.remove(it)) {
                removed = true
            }
        }

        return removed
    }

    override val size: Int
        get() = items.size

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        var changed = false
        for (element in elements) {
            changed = add(element)
        }

        return changed
    }

    override fun add(index: Int, element: T) {
        if (!items.contains(element)) {
            items.add(index, element)
            onAdded.invoke(element)
        }
    }

    override fun clear() {
        if (items.isNotEmpty()) {
            removeAll(items)
        }
    }

    override fun containsAll(elements: Collection<T>) = items.containsAll(elements)

    override fun contains(element: T) = items.contains(element)

    override fun get(index: Int) = items.get(index)
    override fun isEmpty() = items.isEmpty()

    override fun iterator() = items.iterator()

    override fun listIterator() = items.listIterator()

    override fun listIterator(index: Int) = items.listIterator(index)

    override fun lastIndexOf(element: T) = items.lastIndexOf(element)

    override fun indexOf(element: T) = items.indexOf(element)

    override fun removeAt(index: Int): T {
        val item = items.removeAt(index)
        onRemoved.invoke(item)

        return item
    }

    override fun subList(fromIndex: Int, toIndex: Int) = items.subList(fromIndex, toIndex)

    override fun retainAll(elements: Collection<T>): Boolean {
        val elementsToRemove = items - elements
        return removeAll(elementsToRemove)
    }

    override fun set(index: Int, element: T): T {
        val old = items.set(index, element)
        onRemoved.invoke(old)
        onAdded.invoke(element)

        return old
    }
}