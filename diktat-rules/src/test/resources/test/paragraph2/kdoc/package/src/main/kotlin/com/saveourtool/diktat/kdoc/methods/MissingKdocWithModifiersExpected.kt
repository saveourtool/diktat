package test.paragraph2.kdoc

class Example {
    /**
 * @param a
 * @param b
 * @return
 * @throws IllegalStateException
 */
@Deprecated("Use something else")
    internal fun addInts(a: Int, b: Int): Int {
        if (false) throw IllegalStateException()
        return a + b
    }
}
