package test.paragraph2.kdoc

/**
 * Converts this AST node and all its children to pretty string representation
 */
@Suppress("AVOID_NESTED_FUNCTIONS")
fun Example.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
    /**
     * AST operates with \n only, so we need to build the whole string representation and then change line separator
     */
    fun Example.doPrettyPrint(level: Int, maxLevel: Int): String {
        return "test" + level + maxLevel
    }
    return doPrettyPrint(level, maxLevel).replace("\n", System.lineSeparator())
}

/**
 * right place for kdoc
 */
class Example {
    /**
     * right place for kdoc
     */
    fun doGood() {
        /*
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
