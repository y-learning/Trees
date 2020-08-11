package redblack

import list.List
import list.concat
import list.sequence
import result.Result

class MapEntry<K : Any, V> private constructor(
        private val key: K, val value: Result<V>) : Comparable<MapEntry<K, V>> {

    override fun compareTo(other: MapEntry<K, V>): Int =
            hashCode().compareTo(other.hashCode())

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
        private val delegate: Tree<MapEntry<Int, List<Pair<K, V>>>> = Tree()) {

    private fun getAll(key: @UnsafeVariance K): Result<List<Pair<K, V>>> =
            delegate[MapEntry(key.hashCode())].flatMap { mapEntry ->
                mapEntry.value.map { it }
            }


    operator fun plus(entry: Pair<@UnsafeVariance K, V>): Map<K, V> {
        val list = getAll(entry.first).map { list ->
            list.foldLeft(List(entry)) { acc ->
                { pair: Pair<K, V> ->
                    if (pair.first == entry.first) acc
                    else acc.cons(entry)
                }
            }
        }.getOrElse(List(entry))

        return Map(delegate + MapEntry(entry.first.hashCode(), list))
    }

    operator fun minus(key: @UnsafeVariance K): Map<K, V> {
        val list = getAll(key).map { list ->
            list.foldLeft(List<Pair<K, V>>()) { acc ->
                { pair ->
                    if (pair.first == key) acc
                    else acc.cons(pair)
                }
            }
        }.getOrElse(List())

        return when {
            list.isEmpty() -> Map(delegate - MapEntry(key.hashCode()))
            else -> Map(delegate + MapEntry(key.hashCode(), list))
        }
    }

    fun contains(key: @UnsafeVariance K): Boolean =
            getAll(key).map { list: List<Pair<K, V>> ->
                list.exists { pair ->
                    pair.first == key
                }
            }.getOrElse(false)

    fun get(key: @UnsafeVariance K): Result<Pair<K, V>> = getAll(key)
            .flatMap { list ->
                list.filter { pair ->
                    pair.first == key
                }.firstSafe()
            }

    fun isEmpty(): Boolean = delegate.isEmpty

    fun size(): Int = delegate.size

    fun values(): List<V> = sequence(delegate.foldInReverseOrder(List())
    { left: List<Result<V>> ->
        { mapEntry: MapEntry<Int, List<Pair<K, V>>> ->
            { right: List<Result<V>> ->
                right.concat(mapEntry.value.map {
                    it.map { pair -> Result(pair.second) }
                }.getOrElse(List()).concat(left))
            }
        }
    }).getOrElse(List())

    fun <B> foldLeft(identity: B,
                     f: (B) -> (MapEntry<@UnsafeVariance K, V>) -> B,
                     g: (B) -> (B) -> B): B = delegate.foldLeft(identity,
            { acc: B ->
                { mapEntry: MapEntry<Int, List<Pair<K, V>>> ->
                    mapEntry.value.map { list: List<Pair<K, V>> ->
                        list.map { pair: Pair<K, V> ->
                            MapEntry(pair.first, pair.second)
                        }
                    }.map {
                        g(acc)(it.foldLeft(identity, f))
                    }.getOrElse(identity)
                }
            }, g)

    override fun toString(): String = delegate.toString()

    companion object {
        operator fun invoke(): Map<Nothing, Nothing> = Map()
    }
}
