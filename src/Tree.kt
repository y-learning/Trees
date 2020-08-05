import list.List
import kotlin.math.max

sealed class Tree<out E : Comparable<@UnsafeVariance E>> {

    abstract fun isEmpty(): Boolean

    abstract val size: Int

    abstract val height: Int

    fun contains(e: @UnsafeVariance E): Boolean = when (this) {
        Empty -> false
        is T -> when {
            e < root -> left.contains(e)
            e > root -> right.contains(e)
            else -> true
        }
    }

    operator fun plus(element: @UnsafeVariance E): Tree<E> = when (this) {
        Empty -> T(Empty, element, Empty)
        is T -> when {
            element > this.root -> T(left, root, right + element)
            element < this.root -> T(left + element, root, right)
            else -> T(this.left, element, right)
        }
    }

    internal object Empty : Tree<Nothing>() {
        override fun isEmpty(): Boolean = true

        override val size: Int = 0

        override val height: Int = -1

        override fun toString(): String = "E"
    }

    internal data class T<out E : Comparable<@UnsafeVariance E>>(
        internal val left: Tree<E>,
        internal val root: E,
        internal val right: Tree<E>
    ) : Tree<E>() {

        override fun isEmpty(): Boolean = false

        override val size: Int = 1 + left.size +right.size

        override val height: Int = 1 + max(left.height , right.height)

        override fun toString(): String = "(T $left $root $right)"
    }

    companion object {

        operator fun <E : Comparable<E>> invoke(): Tree<E> = Empty

        operator fun <E : Comparable<E>> invoke(vararg az: E): Tree<E> =
            az.fold(Empty) { acc: Tree<E>, e: E -> acc + e }

        operator fun <E : Comparable<E>> invoke(list: List<E>): Tree<E> =
            list.foldLeft<Tree<E>>(Empty) { acc: Tree<E> ->
                { e: E ->
                    acc + e
                }
            }
    }
}