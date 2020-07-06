package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.nextSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.junit.Assert
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

    @Test
    fun `moveChildBefore 1 - Should correctly move node child before another`() {
        applyToCode("""
                |val a = 0
                |val b = 1
            """.trimMargin()) { node ->
            if (node.elementType == CLASS_BODY) {
                val val1 = node.getFirstChildWithType(PROPERTY)
                val val2 = val1!!.nextSibling { it.elementType == PROPERTY }!!
                val whiteSpace = val1.treeNext.clone() as ASTNode
                node.moveChildBefore(val2, val1, false)
                node.addChild(whiteSpace, val1)
                node.removeChild(val1.treeNext)
                Assert.assertTrue(node.text == """
                    |val b = 1
                    |val a = 0
                    |
                    """.trimMargin()
                )
            }
        }
    }

    @Test
    fun `moveChildBefore 2 - Should correctly move node child to the end`() {
        applyToCode("""
                |val a = 0
                |val b = 1
            """.trimMargin()) { node ->
            if (node.elementType == FILE) {
                val val1 = node.getFirstChildWithType(PROPERTY)!!
                val whiteSpace = val1.treeNext.clone() as ASTNode
                node.moveChildBefore(val1, null, false)
                node.addChild(whiteSpace, node.getChildren(null).last())
                Assert.assertTrue(node.text == """
                    |
                    |val b = 1
                    |val a = 0
                    """.trimMargin()
                )
            }
        }
    }

    @Test
    fun `moveChildBefore 3 - Should correctly move node child to the end`() {
        applyToCode("""
                |val a = 0
                |val b = 1""".trimMargin()) { node ->
            if (node.elementType == FILE) {
                val val1 = node.getFirstChildWithType(PROPERTY)!!
                val val2 = val1.nextSibling { it.elementType == PROPERTY }!!
                node.moveChildBefore(val2, val1, true)
                node.addChild(PsiWhiteSpaceImpl("\n"), val1)
                Assert.assertTrue(node.text == """
                    |val b = 1
                    |val a = 0
                    |
                    """.trimMargin()
                )
            }
        }
    }

    private fun applyToCode(code: String, applyToNode: (node: ASTNode) -> Unit) {
        KtLint.lint(
                KtLint.Params(
                        text = code,
                        ruleSets = listOf(
                                RuleSet("test", object : Rule("astnode-utils-test") {
                                    override fun visit(node: ASTNode,
                                                       autoCorrect: Boolean,
                                                       params: KtLint.Params,
                                                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
                                        applyToNode(node)
                                    }
                                })
                        ),
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
