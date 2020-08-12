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

    operator fun plus(e: @UnsafeVariance E): Heap<E> = merge(this, Heap(e))

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
            override val rank: Int,
            private val l: Heap<E>,
            private val h: E,
            private val r: Heap<E>) : Heap<E>() {

        override val head: Result<E> = Result(h)

        override val left: Result<Heap<E>> = Result(l)

        override val right: Result<Heap<E>> = Result(r)

        override val size: Int = 1 + l.size + r.size

        override val isEmpty: Boolean = false

        override fun toString(): String = "(T $l $h $r)"
    }

    companion object {
        operator fun <E : Comparable<E>> invoke(): Heap<E> = E

        operator fun <E : Comparable<E>> invoke(e: E): Heap<E> = H(1, E, e, E)

        private fun <E : Comparable<E>> make(head: E,
                                             first: Heap<E>,
                                             second: Heap<E>): Heap<E> = when {
            first.rank >= second.rank -> H(second.rank + 1, first, head, second)
            else -> H(first.rank + 1, second, head, first)
        }

        fun <E : Comparable<E>> merge(heap1: Heap<E>, heap2: Heap<E>): Heap<E> =
                heap1.head.flatMap { head1: E ->
                    heap2.head.flatMap { head2: E ->
                        when {
                            head1 <= head2 -> {
                                heap1.left.flatMap { left1: Heap<E> ->
                                    heap1.right.map { right1: Heap<E> ->
                                        make(head1, left1, merge(right1, heap2))
                                    }
                                }
                            }

                            else -> {
                                heap2.left.flatMap { left2: Heap<E> ->
                                    heap2.right.map { right2: Heap<E> ->
                                        make(head2, left2, merge(right2, heap1))
                                    }
                                }
                            }
                        }
                    }
                }.getOrElse(when (heap1) {
                    E -> heap2
                    else -> heap1
                })
    }
}