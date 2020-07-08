package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import com.pinterest.ktlint.core.ast.nextSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.junit.Assert
import org.junit.Test

class ASTNodeUtilsTest {
    @Test
    fun `String representation of ASTNode`() {
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        PrettyPrintingVisitor.assertStringRepr(FILE, code, 0, 2, """
            |kotlin.FILE: "class Test {
            |    val x = 0
            |}"
            |- PACKAGE_DIRECTIVE: ""
            |- IMPORT_LIST: ""
            |- CLASS: "class Test {
            |    val x = 0
            |}"
            |-- class: "class"
            |-- WHITE_SPACE: " "
            |-- IDENTIFIER: "Test"
            |-- WHITE_SPACE: " "
            |-- CLASS_BODY: "{
            |    val x = 0
            |}"
            |
        """.trimMargin())

        PrettyPrintingVisitor.assertStringRepr(FILE, """val x = 0""", expected = """
            |kotlin.FILE: "val x = 0"
            |- PACKAGE_DIRECTIVE: ""
            |- IMPORT_LIST: ""
            |- PROPERTY: "val x = 0"
            |-- val: "val"
            |-- WHITE_SPACE: " "
            |-- IDENTIFIER: "x"
            |-- WHITE_SPACE: " "
            |-- EQ: "="
            |-- WHITE_SPACE: " "
            |-- INTEGER_CONSTANT: "0"
            |--- INTEGER_LITERAL: "0"
            |
        """.trimMargin())
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

    @Test
    fun `isChildAfterGroup test`() {
        applyToCode("val x = 0") { node ->
            if (node.elementType == PROPERTY) {
                val valNode = node.getFirstChildWithType(VAL_KEYWORD)!!
                val identifier = node.getFirstChildWithType(IDENTIFIER)!!
                val eq = node.getFirstChildWithType(EQ)!!
                val zero = node.getFirstChildWithType(INTEGER_CONSTANT)!!

                Assert.assertTrue(node.isChildAfterAnother(zero, valNode))
                Assert.assertTrue(node.isChildAfterGroup(zero, listOf(identifier, eq)))

                Assert.assertTrue(node.isChildBeforeAnother(identifier, zero))
                Assert.assertTrue(node.isChildBeforeGroup(identifier, listOf(eq, zero)))
                Assert.assertTrue(node.areChildrenBeforeChild(listOf(valNode, identifier, eq), zero))
                Assert.assertTrue(node.areChildrenBeforeGroup(listOf(valNode, identifier), listOf(eq, zero)))
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

private class PrettyPrintingVisitor(private val elementType: IElementType,
                                    private val level: Int,
                                    private val maxLevel: Int,
                                    private val expected: String) : Rule("print-ast") {
    companion object {
        fun assertStringRepr(elementType: IElementType, code: String, level: Int = 0, maxLevel: Int = -1, expected: String) {
            KtLint.lint(
                    KtLint.Params(
                            text = code,
                            ruleSets = listOf(RuleSet("test", PrettyPrintingVisitor(elementType, level, maxLevel, expected))),
                            cb = { _, _ -> Unit }
                    )
            )
        }
    }

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node.elementType == elementType) {
            Assert.assertEquals(
                    expected.replace("\n", System.lineSeparator()),
                    node.prettyPrint(level, maxLevel)
            )
        }
    }
}
