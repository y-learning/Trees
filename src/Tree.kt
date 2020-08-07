import list.List
import list.concat
import result.Result
import kotlin.math.max

sealed class Tree<out E : Comparable<@UnsafeVariance E>> {

    abstract fun isEmpty(): Boolean

    abstract val size: Int

    abstract val height: Int

    abstract fun max(): Result<E>

    abstract fun min(): Result<E>

    abstract fun merge(tree: Tree<@UnsafeVariance E>): Tree<E>

    abstract fun <T> foldLeft(identity: T,
                              f: (T) -> (E) -> T,
                              g: (T) -> (T) -> T): T

    abstract fun <T> foldRight(identity: T,
                               f: (E) -> (T) -> T,
                               g: (T) -> (T) -> T): T

    abstract fun <T> foldInOrder(identity: T, f: (T) -> (E) -> (T) -> T): T

    abstract fun <T> foldPreOrder(identity: T, f: (E) -> (T) -> (T) -> T): T

    abstract fun <T> foldPostOrder(identity: T, f: (T) -> (T) -> (E) -> T): T

    abstract fun toListPreOrderLeft(): List<E>

    abstract fun <T> foldLeft(identity: T, f: (T) -> (E) -> T): T

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

    fun removeMerge(tree: Tree<@UnsafeVariance E>): Tree<E> = when (this) {
        Empty -> tree
        is T -> when (tree) {
            Empty -> this
            is T -> when {
                tree.root < root -> T(left.removeMerge(tree), root, right)
                else -> T(left, root, right.removeMerge(tree))
            }
        }
    }

    fun remove(e: @UnsafeVariance E): Tree<E> = when (this) {
        Empty -> this
        is T -> when {
            e < root -> T(left.remove(e), root, right)
            e > root -> T(left, root, right.remove(e))
            else -> left.removeMerge(right)
        }
    }

    internal object Empty : Tree<Nothing>() {
        override fun isEmpty(): Boolean = true

        override val size: Int = 0

        override val height: Int = -1

        override fun max(): Result<Nothing> = Result()

        override fun min(): Result<Nothing> = Result()

        override fun merge(tree: Tree<Nothing>): Tree<Nothing> = tree

        override fun <T> foldLeft(identity: T,
                                  f: (T) -> (Nothing) -> T,
                                  g: (T) -> (T) -> T): T = identity

        override fun <T> foldRight(identity: T,
                                   f: (Nothing) -> (T) -> T,
                                   g: (T) -> (T) -> T): T = identity

        override
        fun <T> foldInOrder(identity: T, f: (T) -> (Nothing) -> (T) -> T): T =
            identity

        override
        fun <T> foldPreOrder(identity: T, f: (Nothing) -> (T) -> (T) -> T): T =
            identity

        override
        fun <T> foldPostOrder(identity: T, f: (T) -> (T) -> (Nothing) -> T): T =
            identity

        override fun toListPreOrderLeft(): List<Nothing> = List()

        override fun <T> foldLeft(identity: T, f: (T) -> (Nothing) -> T): T =
            identity

        override fun toString(): String = "E"
    }

    internal data class T<out E : Comparable<@UnsafeVariance E>>(
        internal val left: Tree<E>,
        internal val root: E,
        internal val right: Tree<E>
    ) : Tree<E>() {

        override fun isEmpty(): Boolean = false

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + max(left.height, right.height)

        override fun max(): Result<E> = right.max().orElse { Result(root) }

        override fun min(): Result<E> = left.min().orElse { Result(root) }

        override fun merge(tree: Tree<@UnsafeVariance E>): Tree<E> =
            when (tree) {
                Empty -> this
                is T -> when {
                    tree.root > root -> {
                        val treeRight = T(Empty, tree.root, tree.right)
                        T(left, root, right.merge(treeRight)).merge(tree.left)
                    }

                    tree.root < root -> {
                        val treeLeft = T(tree.left, tree.root, Empty)
                        T(left.merge(treeLeft), root, right).merge(tree.right)
                    }
                    else ->
                        T(left.merge(tree.left), root, right.merge(tree.right))
                }
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

        override fun toListPreOrderLeft(): List<E> =
            left.toListPreOrderLeft()
                .concat(right.toListPreOrderLeft())
                .cons(root)

        override fun <T> foldLeft(identity: T, f: (T) -> (E) -> T): T =
            toListPreOrderLeft().foldLeft(identity, f)

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

        operator fun <E : Comparable<E>> invoke(tree1: Tree<E>,
                                                root: E,
                                                tree2: Tree<E>): Tree<E> {
            fun isOrdered(left: Tree<E>, root: E, right: Tree<E>): Boolean =
                left.max().flatMap { lMax: E ->
                    right.min().map { rMin: E -> lMax < root && root < rMin }
                }.getOrElse(left.isEmpty() && right.isEmpty()) ||
                    left.min()
                        .mapEmptyToSuccess()
                        .flatMap { right.min().map { rMin: E -> root < rMin } }
                        .getOrElse(false) ||
                    right.min()
                        .mapEmptyToSuccess()
                        .flatMap { left.max().map { lMax: E -> lMax < root } }
                        .getOrElse(false)

            return when {
                isOrdered(tree1, root, tree2) -> T(tree1, root, tree2)
                isOrdered(tree2, root, tree1) -> T(tree2, root, tree1)
                else -> Tree(root).merge(tree1).merge(tree2)
            }
        }
    }
}