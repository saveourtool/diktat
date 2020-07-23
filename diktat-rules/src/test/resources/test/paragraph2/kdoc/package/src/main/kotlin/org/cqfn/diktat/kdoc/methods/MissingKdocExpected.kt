package org.cqfn.diktat.kdoc.methods

class Example {
    /**
     * @param a
     * @param b
     * @return
     * @throws IllegalStateException
     */
    fun addInts(a: Int, b: Int): Int {
        if (false) throw IllegalStateException()
        return a + b
    }
}
