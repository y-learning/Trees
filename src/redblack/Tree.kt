package redblack

import redblack.Color.*
import redblack.Tree.E
import result.Result
import java.lang.Integer.max

sealed class Tree<out E : Comparable<@UnsafeVariance E>> {
    abstract val size: Int
    abstract val height: Int
    abstract val isEmpty: Boolean

    internal abstract val color: Color
    internal abstract val isBlack: Boolean
    internal abstract val isBB: Boolean
    internal abstract val isRedTree: Boolean
    internal abstract val isBlackTree: Boolean
    internal abstract val isTNB: Boolean

    abstract fun max(): Result<E>

    abstract fun min(): Result<E>

    abstract fun contains(e: @UnsafeVariance E): Boolean

    abstract fun <T> foldLeft(identity: T,
                              f: (T) -> (E) -> T,
                              g: (T) -> (T) -> T): T

    abstract fun <T> foldRight(identity: T,
                               f: (E) -> (T) -> T,
                               g: (T) -> (T) -> T): T

    abstract fun <T> foldInOrder(identity: T, f: (T) -> (E) -> (T) -> T): T

    abstract fun <T> foldPreOrder(identity: T, f: (E) -> (T) -> (T) -> T): T

    abstract fun <T> foldPostOrder(identity: T, f: (T) -> (T) -> (E) -> T): T

    internal abstract fun redder(): Tree<E>
    internal abstract fun add(e: @UnsafeVariance E): Tree<E>
    internal abstract fun delete(e: @UnsafeVariance E): Tree<E>
    protected abstract fun blacken(): Tree<E>
    protected abstract fun removeMax(): Tree<E>

    protected fun balance(
            color: Color,
            left: Tree<@UnsafeVariance E>,
            root: @UnsafeVariance E,
            right: Tree<@UnsafeVariance E>): Tree<E> = when (color) {
        Black -> when {
            left is T && left.isRedTree -> when {
                left.left.isRedTree -> T(Red, left.left.blacken(), left.root,
                        T(Black, left.right, root, right))

                left.right is T && left.right.isRedTree ->
                    T(Red, T(Black, left.left, left.root, left.right.left),
                            left.right.root,
                            T(Black, left.right.right, root, right))

                else -> T(color, left, root, right)
            }

            right is T && right.isRedTree -> when {
                right.right.isRedTree -> T(Red,
                        T(Black, left, root, right.left), right.root,
                        right.right.blacken())

                right.left is T && right.left.isRedTree -> T(Red,
                        T(Black, left, root, right.left.left), right.left.root,
                        T(Black, right.left.right, right.root, right.right))

                else -> T(color, left, root, right)
            }

            else -> T(color, left, root, right)
        }

        else -> T(color, left, root, right)
    }

    operator fun plus(e: @UnsafeVariance E): Tree<E> = add(e).blacken()

    operator fun minus(e: @UnsafeVariance E): Tree<E> = delete(e).blacken()

    abstract operator fun get(e: @UnsafeVariance E): Result<E>

    internal
    abstract class Empty<out E : Comparable<@UnsafeVariance E>> : Tree<E>() {
        override val size: Int = 0

        override val height: Int = -1

        override val isEmpty: Boolean = true

        override val isRedTree: Boolean = false

        override val isBlackTree: Boolean = false

        override val isTNB: Boolean = false

        override fun max(): Result<E> = Result()

        override fun min(): Result<E> = Result()

        override fun contains(e: @UnsafeVariance E): Boolean = false

        override fun <T> foldLeft(identity: T,
                                  f: (T) -> (E) -> T,
                                  g: (T) -> (T) -> T): T = identity

        override fun <T> foldRight(identity: T,
                                   f: (E) -> (T) -> T,
                                   g: (T) -> (T) -> T): T = identity

        override fun <T> foldInOrder(identity: T,
                                     f: (T) -> (E) -> (T) -> T): T = identity

        override fun <T> foldPreOrder(identity: T,
                                      f: (E) -> (T) -> (T) -> T): T = identity

        override fun <T> foldPostOrder(identity: T,
                                       f: (T) -> (T) -> (E) -> T): T = identity

        override fun get(e: @UnsafeVariance E): Result<E> = Result()

        override fun add(e: @UnsafeVariance E): Tree<E> = T(Red, E, e, E)

        override fun delete(e: @UnsafeVariance E): Tree<E> = E

        override fun blacken(): Tree<E> = E

        override fun removeMax(): Tree<E> = this
    }

    internal object E : Empty<Nothing>() {
        override val color: Color = Red

        override val isBlack: Boolean = true

        override val isBB: Boolean = false

        override fun redder(): Tree<Nothing> = this

        override fun toString(): String = "E"
    }

    internal object EE : Empty<Nothing>() {
        override val color: Color = BB

        override val isBlack: Boolean = false

        override val isBB: Boolean = true

        override fun redder(): Tree<Nothing> = E

        override fun toString(): String = "EE"
    }

    internal class T<out E : Comparable<@UnsafeVariance E>>(
            override val color: Color,
            internal val left: Tree<E>,
            internal val root: E,
            internal val right: Tree<E>) : Tree<E>() {

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + max(left.height, right.height)

        override val isEmpty: Boolean = false

        override val isRedTree: Boolean = color == Red

        override val isBlack: Boolean = color == Black

        override val isBB: Boolean = color == BB

        override val isTNB: Boolean = color == NB

        override val isBlackTree: Boolean = color == Black || color == BB

        override fun max(): Result<E> = when {
            right.isEmpty -> Result(root)
            else -> right.max()
        }

        override fun min(): Result<E> = when {
            left.isEmpty -> Result(root)
            else -> left.min()
        }

        override fun contains(e: @UnsafeVariance E): Boolean = when {
            e > root -> right.contains(e)
            e < root -> left.contains(e)
            else -> true
        }

        override fun <T> foldLeft(identity: T,
                                  f: (T) -> (E) -> T,
                                  g: (T) -> (T) -> T): T =
                g(right.foldLeft(identity, f, g))(f(left
                        .foldLeft(identity, f, g))(root))

        override fun <T> foldRight(identity: T,
                                   f: (E) -> (T) -> T,
                                   g: (T) -> (T) -> T): T =
                g(f(root)(left.foldRight(identity, f, g)))(right
                        .foldRight(identity, f, g))

        override
        fun <T> foldInOrder(identity: T, f: (T) -> (E) -> (T) -> T): T =
                f(left.foldInOrder(identity, f))(root)(right
                        .foldInOrder(identity, f))

        override
        fun <T> foldPreOrder(identity: T, f: (E) -> (T) -> (T) -> T): T =
                f(root)(left.foldPreOrder(identity, f))(right
                        .foldPreOrder(identity, f))

        override
        fun <T> foldPostOrder(identity: T, f: (T) -> (T) -> (E) -> T): T =
                f(left.foldPostOrder(identity, f))(right
                        .foldPostOrder(identity, f))(root)

        override fun get(e: @UnsafeVariance E): Result<E> = when {
            e > root -> right[e]
            e < root -> left[e]
            else -> Result(root)
        }

        override fun add(e: @UnsafeVariance E): Tree<E> = when {
            e < root -> balance(color, left.add(e), root, right)
            e > root -> balance(color, left, root, right.add(e))
            else -> T(color, left, e, right)
        }

        private fun bubble(color: Color, left: Tree<E>, value: E,
                           right: Tree<E>): Tree<E> = when {
            left.isBB || right.isBB ->
                balance(color.blacker, left.redder(), value, right.redder())
            else -> balance(color, left, value, right)
        }

        private fun remove(): Tree<E> = when {
            isRedTree && left.isEmpty && right.isEmpty -> E
            isBlackTree && left.isEmpty && right.isEmpty -> EE
            isBlackTree && left.isEmpty && right.isRedTree && right is T ->
                T(Black, right.left, right.root, right.right)
            isBlackTree && left.isRedTree && right.isEmpty && left is T ->
                T(Black, left.left, left.root, left.right)
            left.isEmpty && right is T ->
                bubble(right.color, right.left, right.root, right.right)
            else -> left.max().map { max: E ->
                bubble(color, left.removeMax(), max, right)
            }.getOrElse(E)
        }

        override fun delete(e: @UnsafeVariance E): Tree<E> = when {
            e < root -> bubble(color, left.delete(e), root, right)
            e > root -> bubble(color, left, root, right.delete(e))
            else -> remove()
        }

        override fun blacken(): Tree<E> = T(Black, left, root, right)

        override fun redder(): Tree<E> = T(color.redder, left, root, right)

        override fun removeMax(): Tree<E> = when {
            right.isEmpty -> remove()
            else -> bubble(color, left, root, right.removeMax())
        }

        override fun toString(): String = "(T $color $left $root $right)"
    }

    companion object {
        operator fun <E : Comparable<E>> invoke(): Tree<E> = E
    }
}