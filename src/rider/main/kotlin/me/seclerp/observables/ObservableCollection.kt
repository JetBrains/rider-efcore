package me.seclerp.observables

class ObservableCollection<T : Any> {
    private val items = mutableListOf<T?>()

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

    fun add(item: T?) {
        if (!items.contains(item)) {
            items.add(item)
            onAdded.invoke(item)
        }
    }

    fun addAll(otherItems: Collection<T?>) {
        otherItems.forEach {
            this.add(it)
        }
    }

    fun remove(item: T?) {
        if (items.contains(item)) {
            items.remove(item)
            onRemoved.invoke(item)
        }
    }

    fun removeAll(otherItems: Collection<T?>) {
        otherItems.forEach {
            this.remove(it)
        }
    }

    fun clear() {
        if (items.isNotEmpty()) {
            removeAll(items)
        }
    }
}