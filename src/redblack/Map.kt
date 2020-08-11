package redblack

import result.Result

class MapEntry<K : Comparable<@UnsafeVariance K>, V> private constructor(
        private val key: K, val value: Result<V>) : Comparable<MapEntry<K, V>> {

    override
    fun compareTo(other: MapEntry<K, V>): Int = key.compareTo(other.key)

    override fun equals(other: Any?): Boolean = (this === other) ||
            when (other) {
                is MapEntry<*, *> -> key == other.key
                else -> false
            }

    override fun hashCode(): Int = key.hashCode()

    override fun toString(): String = "[$key $value]"

    companion object {
        operator
        fun <K : Comparable<K>, V> invoke(key: K, value: V): MapEntry<K, V> =
                MapEntry(key, Result(value))

        operator
        fun <K : Comparable<K>, V> invoke(pair: Pair<K, V>): MapEntry<K, V> =
                invoke(pair.first, pair.second)

        operator fun <K : Comparable<K>, V> invoke(key: K): MapEntry<K, V> =
                MapEntry(key, Result())
    }
}

class Map<out K : Comparable<@UnsafeVariance K>, V>(
        private val delegate: Tree<MapEntry<K, V>> = Tree()) {

    operator fun plus(entry: MapEntry<@UnsafeVariance K, V>): Map<K, V> =
            Map(delegate + entry)

    operator fun minus(key: @UnsafeVariance K): Map<K, V> =
            Map(delegate - MapEntry(key))

    fun contains(key: @UnsafeVariance K): Boolean =
            delegate.contains(MapEntry(key))

    fun get(key: @UnsafeVariance K): Result<MapEntry<@UnsafeVariance K, V>> =
            delegate[MapEntry(key)]

    fun isEmpty(): Boolean = delegate.isEmpty

    fun size(): Int = delegate.size

    override fun toString(): String = delegate.toString()

    companion object {
        operator fun invoke(): Map<Nothing, Nothing> = Map()
    }
}
