package redblack

sealed class Color {
    internal object Red : Color() {
        override fun toString(): String = "R"
    }

    internal object Black : Color() {
        override fun toString(): String = "B"
    }
}
