package org.cqfn.diktat.kdoc.methods

class Example {
    /**
     * Lorem ipsum
     */
    fun test1(a: Int): Unit { }

    /**
     * Lorem ipsum
     * @return integer value
     */
    fun test2(a: Int): Int = 0

    /**
     * Lorem ipsum
     * @throws IllegalStateException
     */
    fun test3(a: Int) {
        throw IllegalStateException("dolor sit amet")
    }

    /**
     * Lorem ipsum
     * @return integer value
     * @throws IllegalStateException
     */
    fun test4(a: Int): Int {
        throw IllegalStateException("dolor sit amet")
        return 0
    }

    /**
     * Lorem ipsum
     * @return integer value
     * @throws IllegalStateException
     */
    fun test5(a: Int, b: String): Int {
        throw IllegalStateException("dolor sit amet")
        return 0
    }
}
