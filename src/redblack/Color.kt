package redblack

sealed class Color {
    internal abstract val blacker: Color
    internal abstract val redder: Color

    internal object Red : Color() {
        override val blacker: Color = Black

        override val redder: Color = NB

        override fun toString(): String = "R"
    }

    internal object Black : Color() {
        override val blacker: Color = BB

        override val redder: Color = Red

        override fun toString(): String = "B"
    }

    /** DoubleBlack */
    internal object BB : Color() {

        override val blacker: Color by lazy<Color> {
            throw IllegalStateException("Can't make DoubleBlack blacker")
        }

        override val redder: Color = Black

        override fun toString(): String = "BB"
    }

    /** NegativeBlack */
    internal object NB : Color() {

        override val blacker: Color = Red

        override val redder: Color by lazy<Color> {
            throw IllegalStateException("Can't make NegativeBlack redder")
        }

        override fun toString(): String = "NB"
    }
}
