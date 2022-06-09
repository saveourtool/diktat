package test.paragraph2.kdoc

import org.jetbrains.kotlin.com.intellij.lang.ASTNode

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
        fun prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
            return "test"
        }
    }
}
/**
 * Converts this AST node and all its children to pretty string representation
 */
@Suppress("AVOID_NESTED_FUNCTIONS")
fun ASTNode.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
    /**
     * AST operates with \n only, so we need to build the whole string representation and then change line separator
     */
    fun ASTNode.doPrettyPrint(level: Int, maxLevel: Int): String {
        val result = StringBuilder("${this.elementType}: \"${this.text}\"").append('\n')
        if (maxLevel != 0) {
            this.getChildren(null).forEach { child ->
                result.append(
                    "${"-".repeat(level + 1)} " +
                            child.doPrettyPrint(level + 1, maxLevel - 1)
                )
            }
        }
        return result.toString()
    }
    return doPrettyPrint(level, maxLevel).replace("\n", System.lineSeparator())
}
