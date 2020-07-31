sealed class Tree<out E : Comparable<@UnsafeVariance E>> {

    abstract fun isEmpty(): Boolean

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

        override fun toString(): String = "E"
    }

    internal class T<out E : Comparable<@UnsafeVariance E>>(
        internal val left: Tree<E>,
        internal val root: E,
        internal val right: Tree<E>
    ) : Tree<E>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "(T $left $root $right)"
    }

    companion object {

        operator fun <E : Comparable<E>> invoke(): Tree<E> = Empty
    }
}