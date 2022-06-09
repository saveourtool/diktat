package test.paragraph2.kdoc

/**
 * right place for kdoc
 */
class Example {
    /**
     * right place for kdoc
     */
    fun doGood() {
        /**
         * wrong place for kdoc
         */
        /*
         * right place for block comment
        */
        // right place for eol comment
        1 + 2
        /**
         * Converts this AST node and all its children to pretty string representation
         */
        fun Example.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
            return "test"
        }
    }
}