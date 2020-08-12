package leftistheap

import leftistheap.Heap.E
import result.Result

sealed class Heap<out E : Comparable<@UnsafeVariance E>> {
    internal abstract val head: Result<E>
    internal abstract val left: Result<Heap<E>>
    internal abstract val right: Result<Heap<E>>

    protected abstract val rank: Int

    abstract val size: Int
    abstract val isEmpty: Boolean

    abstract class Empty<out E : Comparable<@UnsafeVariance E>> : Heap<E>() {
        override
        val head: Result<E> = Result.failure("head() called on empty heap")

        override val left: Result<Heap<E>> = Result(E)

        override val right: Result<Heap<E>> = Result(E)

        override val rank: Int = 0

        override val size: Int = 0

        override val isEmpty: Boolean = true

        override fun toString(): String = "E"
    }

    internal object E : Empty<Nothing>()

    internal class H<out E : Comparable<@UnsafeVariance E>>(
            override val rank: Int, l: Heap<E>, h: E, r: Heap<E>) : Heap<E>() {

        override val head: Result<E> = Result(h)

        override val left: Result<Heap<E>> = Result(l)

        override val right: Result<Heap<E>> = Result(r)

        override val size: Int = 1 + l.size + r.size

        override val isEmpty: Boolean = false

        override fun toString(): String = "(T $left $head $right)"
    }

    companion object {
        operator fun <E : Comparable<E>> invoke(): Heap<E> = E
    }
}