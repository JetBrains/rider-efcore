package me.seclerp.observables

class ObservableCollection<T : Any>(
    initialCollection: Collection<T?> = listOf()
) : AbstractMutableList<T?>() {
    private val items = initialCollection.toMutableList()

    val value: List<T?>
        get() = items

    val onAdded: Event<T?> = Event()
    val onRemoved: Event<T?> = Event()

    fun afterAdded(effect: (T?) -> Unit) {
        onAdded += effect
    }

    fun afterRemoved(effect: (T?) -> Unit) {
        onRemoved += effect
    }

    override fun add(element: T?): Boolean {
        if (!items.contains(element)) {
            if (items.add(element)) {
                onAdded.invoke(element)
                return true
            }
        }

        return false
    }

    override fun addAll(elements: Collection<T?>): Boolean {
        var changed = false
        elements.forEach {
            if (this.add(it)) {
                changed = true
            }
        }

        return changed
    }

    override fun remove(element: T?): Boolean {
        var removed = false
        if (items.contains(element)) {
            if (items.remove(element)) {
                removed = true
                onRemoved.invoke(element)
            }
        }

        return removed
    }

    override fun removeAll(elements: Collection<T?>): Boolean {
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

    override fun add(index: Int, element: T?) {
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

    override fun get(index: Int) = items.get(index)

    override fun removeAt(index: Int): T? {
        val item = items.removeAt(index)
        onRemoved.invoke(item)

        return item
    }

    override fun set(index: Int, element: T?): T? {
        val old = items.set(index, element)
        onRemoved.invoke(old)
        onAdded.invoke(element)

        return old
    }
}