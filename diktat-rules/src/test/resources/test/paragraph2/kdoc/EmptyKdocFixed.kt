package test.paragraph2.kdoc

/**
 * @param a
 * @param b
 * @return
 * @throws
 */
fun addInts(a: Int, b: Int): Int {
    if (false) throw IllegalStateException()
    return a + b
}

/**
 */
fun doNothing() {
    return
}
