package test.paragraph3.multiple_modifiers

public enum class Q {}

protected data class Counter(val dayIndex: Int) {
    suspend operator fun plus(increment: Int): Counter {
        return Counter(dayIndex + increment)
    }
}

public final fun foo() {
    protected open lateinit var a: List<ASTNode>
}

public internal fun interface Factory {
    public fun create(): List<Int>
}
