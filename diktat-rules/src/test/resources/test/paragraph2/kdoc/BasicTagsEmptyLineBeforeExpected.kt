package test.paragraph2.kdoc

class Example {
    /**
     * @param a integer parameter
     */
    fun test1(a: Int) = Unit

    /**
     * Description
     *
     * @param a integer parameter
     */
    fun test2(a: Int) = Unit

    /**
     * Description
     * @see test2
     *
     * @param a integer parameter
     */
    fun test3(a: Int) = Unit

    /**
     * @param a integer parameter
     */
    fun test4(a: Int) = Unit
}
