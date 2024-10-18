package test.paragraph2.kdoc

class Example {
    /**
     * @param a integer parameter
     */
    fun test1(a: Int) = Unit

    /**
     * Description
     * @param a integer parameter
     */
    fun test2(a: Int) = Unit

    /**
     * Description
     * Description 2
     * @param a integer parameter
     */
    fun test3(a: Int) = Unit

    /**
     * Description
     * @see test2
     * @param a integer parameter
     */
    fun test4(a: Int) = Unit

    /**
     *
     * @param a integer parameter
     */
    fun test5(a: Int) = Unit
}

/**
 * @property a integer parameter
 */
class Example2(val a: Int) {}

/**
 * Description
 * @property a integer parameter
 */
class Example3(val a: Int) {}

/**
 * Description
 * Description 2
 * @property a integer parameter
 */
class Example4(val a: Int) {}

/**
 * Description
 * @see test2
 * @property a integer parameter
 */
class Example5(val a: Int) {}

/**
 *
 * @property a integer parameter
 */
class Example6(val a: Int) {}
