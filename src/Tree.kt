import Tree.T
import list.List
import list.concat
import result.Result
import kotlin.math.abs
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

    protected abstract fun rotateRight(): Tree<E>

    protected abstract fun rotateLeft(): Tree<E>

    fun contains(e: @UnsafeVariance E): Boolean = when (this) {
        Empty -> false
        is T -> when {
            e < root -> left.contains(e)
            e > root -> right.contains(e)
            else -> true
        }
    }

    operator fun plus(e: @UnsafeVariance E): Tree<E> {
        fun plusUnbalanced(e: @UnsafeVariance E, t: Tree<E>): Tree<E> =
            when (t) {
                Empty -> T(Empty, e, Empty)
                is T -> when {
                    e > t.root -> T(t.left, t.root, t.right + e)
                    e < t.root -> T(t.left + e, t.root, t.right)
                    else -> T(t.left, e, t.right)
                }
            }

        return plusUnbalanced(e, this).let {
            when {
                it.height > log2nlz(it.size) * 100 -> balance(it)
                else -> it
            }
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

    fun <T : Comparable<T>> map(f: (E) -> T): Tree<T> =
        foldInOrder(Empty) { t1: Tree<T> ->
            { e: E ->
                { t2: Tree<T> ->
                    T(t1, f(e), t2)
                }
            }
        }

    fun toListInOrderRight(): List<E> = unbalanceRight(List(), this)

    // TODO : Implement toListInOrderLeft(), toListPreOrderRight(),
    //  toListPostOrderRight()

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

        override fun rotateRight(): Tree<Nothing> = this

        override fun rotateLeft(): Tree<Nothing> = this

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

        override fun rotateRight(): Tree<E> = when (left) {
            Empty -> this
            is T -> T(left.left, left.root, T(left.right, root, right))
        }

        override fun rotateLeft(): Tree<E> = when (right) {
            Empty -> this
            is T -> T(T(left, root, right.left), right.root, right.right)
        }

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

        tailrec
        fun <E : Comparable<E>> unbalanceRight(acc: List<E>, tree: Tree<E>):
            List<E> = when (tree) {
            Empty -> acc
            is T -> when (tree.left) {
                Empty -> unbalanceRight(acc.cons(tree.root), tree.right)
                is T -> unbalanceRight(acc, tree.rotateRight())
            }
        }

        private fun <E : Comparable<E>> deltaHeight(tree: T<E>) =
            abs(tree.left.height - tree.right.height)

        private fun <E : Comparable<E>> isUnbalanced(tree: Tree<E>): Boolean =
            when (tree) {
                Empty -> false
                is T -> deltaHeight(tree) > (tree.size - 1) % 2
            }

        private fun <E : Comparable<E>> balanceFirstLvl(tree: Tree<E>):
            Tree<E> = unfold(tree) { t: Tree<E> ->
            when {
                isUnbalanced(t) -> (t as T<E>).let {
                    if (it.right.height < it.left.height)
                        Result(t.rotateRight())
                    else Result(t.rotateLeft())
                }
                else -> Result()
            }
        }

        private fun <E : Comparable<E>> balanceHelper(tree: Tree<E>):
            Tree<E> = when {
            tree is T && tree.height > log2nlz(tree.size) ->
                if (deltaHeight(tree) > 1) balanceHelper(balanceFirstLvl(tree))
                else T(balanceHelper(tree.left), tree.root,
                    balanceHelper(tree.right))
            else -> tree
        }

        fun <E : Comparable<E>> balance(tree: Tree<E>): Tree<E> =
            balanceHelper(tree.toListInOrderRight().foldLeft(Empty)
            { acc: Tree<E> -> { e: E -> T(Empty, e, acc) } })
    }
}

fun log2nlz(n: Int): Int = when (n) {
    0 -> 0
    else -> 31 - Integer.numberOfLeadingZeros(n)
}

fun <E> unfold(e: E, f: (E) -> Result<E>): E {
    tailrec
    fun <E> unfold(pair: Pair<Result<E>, Result<E>>, f: (E) -> Result<E>):
        Pair<Result<E>, Result<E>> = when (val r2 = pair.second) {
        is Result.Success -> unfold(Pair(r2, r2.flatMap { f(it) }), f)
        else -> pair
    }

    return Result(e).let { unfold(Pair(it, it), f).first.getOrElse(e) }
}