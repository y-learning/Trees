sealed class Tree<out E : Comparable<@UnsafeVariance E>> {

    abstract fun isEmpty(): Boolean

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