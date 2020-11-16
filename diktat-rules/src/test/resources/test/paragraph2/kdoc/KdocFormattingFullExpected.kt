package test.paragraph2.kdoc

class Example {

    /**
     * Empty function to test KDocs
     * @apiNote stuff
     *
     * @implSpec spam
     *
     * Another line of description
     *
     * @param a useless integer
     * @return doubled value
     * @throws RuntimeException never
     */
    @Deprecated(message = "Use testNew")
    fun test(a: Int): Int = 2 * a
}

class Foo {
    /**
     * @implNote lorem ipsum
     */
    private fun foo() {}
}
