package com.saveourtool.diktat.kdoc.methods

class Example {
    /**
     * Lorem ipsum
     * @throws IllegalStateException
     */
    fun test1(): Unit {
        throw IllegalStateException("Lorem ipsum")
    }

    /**
     * Lorem ipsum
     * @param a integer parameter
     * @throws IllegalStateException
     */
    fun test2(a: Int): Unit  {
        throw IllegalStateException("Lorem ipsum")
    }

    /**
     * Lorem ipsum
     * @param a integer parameter
     * @return integer value
     * @throws IllegalStateException
     */
    fun test3(a: Int): Int  {
        throw IllegalStateException("Lorem ipsum")
    }

    /**
     * Lorem ipsum
     * @return integer value
     * @throws IllegalStateException
     */
    fun test4(): Int  {
        throw IllegalStateException("Lorem ipsum")
    }

    /**
     * Lorem ipsum
     * @throws IllegalStateException
     * @throws IllegalAccessException
     */
    fun test5() {
        if (true) throw IllegalStateException("Lorem ipsum")
        else throw IllegalAccessException("Dolor sit amet")
    }

    /**
 * @throws RuntimeException
 */
fun test6() {
        try {
            foo()
        } catch (_: NullPointerException) {
            println("NPE!")
        } catch (e: RuntimeException) {
            println("Whoops...")
            throw e
        } catch (e: Error) {
            val x = IllegalStateException()
            println("Nothing to do here")
            throw x
        }
    }
}
