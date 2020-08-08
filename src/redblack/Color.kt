package redblack

sealed class Color {
    internal object Red : Color() {
        override fun toString(): String = "RED"
    }

    internal object Black : Color() {
        override fun toString(): String = "BLACK"
    }
}
