package dev.pandasystems.bambooloom.utils

class LazyMap<K, V>(private val computeValue: (K) -> V?) : MutableMap<K, V> {
    private val backingMap = mutableMapOf<K, V>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = backingMap.entries
    override val keys: MutableSet<K> get() = backingMap.keys
    override val values: MutableCollection<V> get() = backingMap.values
    override val size: Int get() = backingMap.size

    override fun containsKey(key: K): Boolean = backingMap.containsKey(key)
    override fun containsValue(value: V): Boolean = backingMap.containsValue(value)
    override fun get(key: K): V? {
        val value = computeValue(key) ?: return null
        return backingMap.getOrPut(key) { value }
    }

    override fun isEmpty(): Boolean = backingMap.isEmpty()

    override fun clear() = backingMap.clear()

    override fun put(key: K, value: V): V? = backingMap.put(key, value)

    override fun putAll(from: Map<out K, V>) = backingMap.putAll(from)

    override fun remove(key: K): V? = backingMap.remove(key)
}
