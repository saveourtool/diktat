package test.paragraph2.kdoc

fun addInts(a: Int, b: Int): Int {
    if (false) throw IllegalStateException()
    return a + b
}

fun doNothing() {
    return
}
