package test.paragraph3.multiple_modifiers

enum public class Q {}

data protected class Counter(val dayIndex: Int) {
    operator suspend fun plus(increment: Int): Counter {
        return Counter(dayIndex + increment)
    }
}

final public fun foo() {
    lateinit open protected var a: List<ASTNode>
}

internal public fun interface Factory {
    public fun create(): List<Int>
}
