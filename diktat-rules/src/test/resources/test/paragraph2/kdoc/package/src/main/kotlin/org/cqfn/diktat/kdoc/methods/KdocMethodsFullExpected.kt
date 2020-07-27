package org.cqfn.diktat.kdoc.methods

class KdocMethodsFull {
    fun test1() {
        // this function does nothing
    }

    /**
     * This function is described
     * partially.
     * @param a
     * @return
     */
    fun test2(a: Int): Int {
        return 2 * a
    }

    companion object {
        /**
         * @param a
         * @throws IllegalStateException
         */
        fun test3(a: Int) {
            throw IllegalStateException("Lorem ipsum")
        }
    }

    private class Nested {
        /**
         * @param a
         * @param b
         * @return
         */
        fun test4(a: Int, b: Int): Int = 42
    }
}
