package redblack

import redblack.Color.Black
import redblack.Color.Red
import redblack.Tree.E
import java.lang.Integer.max

sealed class Tree<out E : Comparable<@UnsafeVariance E>> {
    abstract val size: Int

    abstract val height: Int

    internal abstract val color: Color

    abstract fun isEmpty(): Boolean

    internal abstract fun isRedTree(): Boolean

    internal abstract fun isBlackTree(): Boolean

    protected abstract fun add(toAdd: @UnsafeVariance E): Tree<E>

    protected abstract fun blacken(): Tree<E>

    protected fun balance(
            color: Color,
            left: Tree<@UnsafeVariance E>,
            root: @UnsafeVariance E,
            right: Tree<@UnsafeVariance E>): Tree<E> = when (color) {
        Black -> when {
            left is T && left.isRedTree() -> when {
                left.left.isRedTree() -> T(Red, left.left.blacken(), left.root,
                        T(Black, left.right, root, right))

                left.right is T && left.right.isRedTree() ->
                    T(Red, T(Black, left.left, left.root, left.right.left),
                            left.right.root,
                            T(Black, left.right.right, root, right))

                else -> T(color, left, root, right)
            }

            right is T && right.isRedTree() -> when {
                right.right.isRedTree() -> T(Red,
                        T(Black, left, root, right.left), right.root,
                        right.right.blacken())

                right.left is T && right.left.isRedTree() -> T(Red,
                        T(Black, left, root, right.left.left), right.left.root,
                        T(Black, right.left.right, right.root, right.right))

                else -> T(color, left, root, right)
            }

            else -> T(color, left, root, right)
        }

        else -> T(color, left, root, right)
    }

    operator fun plus(e: @UnsafeVariance E): Tree<E> = add(e).blacken()

    internal
    abstract class Empty<out E : Comparable<@UnsafeVariance E>> : Tree<E>() {
        override val size: Int = 0

        override val height: Int = -1

        override val color: Color = Black

        override fun isEmpty(): Boolean = true

        override fun add(toAdd: @UnsafeVariance E): Tree<E> =
                T(Red, E, toAdd, E)

        override fun isRedTree(): Boolean = false

        override fun isBlackTree(): Boolean = false

        override fun blacken(): Tree<E> = E

        override fun toString(): String = "E"
    }

    internal object E : Empty<Nothing>()

    internal class T<out E : Comparable<@UnsafeVariance E>>(
            override val color: Color,
            internal val left: Tree<E>,
            internal val root: E,
            internal val right: Tree<E>) : Tree<E>() {

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + max(left.height, right.height)

        override fun isEmpty(): Boolean = false

        override fun add(toAdd: @UnsafeVariance E): Tree<E> = when {
            toAdd < root -> balance(color, left.add(toAdd), root, right)
            toAdd > root -> balance(color, left, root, right.add(toAdd))
            else -> T(color, left, toAdd, right)
        }

        override fun isRedTree(): Boolean = color == Red

        override fun isBlackTree(): Boolean = color == Black

        override fun blacken(): Tree<E> = T(Black, left, root, right)

        override fun toString(): String = "(T $color $left $root $right)"
    }

    companion object {
        operator fun <E : Comparable<E>> invoke(): Tree<E> = E
    }
}