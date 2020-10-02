/*
 * Copyright (c) Your Company Name Here. 2010-2020
 */

package your.name.here

class Example {
    @get:JvmName("getIsValid")
    val isValid = true
    val foo: Int = 1

    /**
     * @param x
     * @param y
     * @return
     */
    fun bar(x: Int, y: Int) = x + y

    /**
     * @param sub
     * @return
     */
    fun String.countSubStringOccurrences(sub: String): Int {
        // println("sub: $sub")
        return this.split(sub).size - 1
    }

    /**
     * @return
     */
    fun String.splitPathToDirs(): List<String> =
            this
                .replace("\\", "/")
                .replace("//", "/")
                .split("/")
}

