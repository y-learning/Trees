package redblack

import redblack.Color.Black
import redblack.Tree.E
import java.lang.Integer.max

sealed class Tree<out E : Comparable<@UnsafeVariance E>> {
    abstract fun isEmpty(): Boolean

    abstract val size: Int

    abstract val height: Int

    internal abstract val color: Color

    internal abstract class Empty<out E : Comparable<@UnsafeVariance E>> :
        Tree<E>() {
        override fun isEmpty(): Boolean = true

        override val color: Color = Black

        override val size: Int = 0

        override val height: Int = -1

        override fun toString(): String = "E"
    }

    internal object E : Empty<Nothing>()

    internal class T<out E : Comparable<@UnsafeVariance E>>(
        override val color: Color,
        internal val left: Tree<E>,
        internal val root: E,
        internal val right: Tree<E>) : Tree<E>() {

        override fun isEmpty(): Boolean = false

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + max(left.height, right.height)

        override fun toString(): String = "T $color $left $root $right"
    }

    companion object {
        operator fun <E : Comparable<E>> invoke(): Tree<E> = E
    }
}