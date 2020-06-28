package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.Ignore
import org.junit.Test

class ASTNodeUtilsTest {
    /**
     * Demonstration of ASTNode#prettyPrint. Example output:
     * source: "class Test"
     * output:
     *  CLASS: "class Test"
     *  - class: "class"
     *  - WHITE_SPACE: " "
     *  - IDENTIFIER: "Test"
     */
    @Test
    @Ignore("This test is for demonstration only, it doesn't actually check anything")
    fun `pretty print ASTNode`() {
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        KtLint.lint(
            KtLint.Params(
                text = code,
                ruleSets = listOf(RuleSet("test", PrettyPrintingVisitor())),
                cb = { _, _ -> Unit }
            )
        )
    }
}

private class PrettyPrintingVisitor : Rule("print-ast") {
    override fun visit(node: ASTNode, autoCorrect: Boolean, params: KtLint.Params, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        println("=== Beginning of node representation ===")
        print(node.prettyPrint(maxLevel = 2))
        println("=== End of node representation ===")
    }
}
