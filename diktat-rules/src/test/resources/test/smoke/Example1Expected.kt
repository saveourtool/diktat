package org.cqfn.diktat.test.smoke

class Example {
    @get:JvmName ("getIsValid")
    val isValid = true

    val foo: Int =1
    /**
     * @param x
     * @param y
     * @return
     */
    fun bar(x: Int, y: Int) = x + y
}

