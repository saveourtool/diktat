package org.cqfn.diktat

class Example {
    @get:JvmName("getIsValid")
    val isValid = true
    val foo: Int = 1

    fun Foo.foo() { }

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
            this.replace("\\", "/").replace("//", "/")
                .split("/")

    /**
     * @param x
     * @param y
     * @return
     */
    fun foo(x: Int,
            y: Int) = x +
            (y +
                    bar(x, y)
            )
}

