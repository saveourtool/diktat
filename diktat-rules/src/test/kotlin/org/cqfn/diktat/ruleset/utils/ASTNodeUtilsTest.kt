package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.nextSibling
import org.cqfn.diktat.util.applyToCode
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.junit.Assert
import org.junit.Test

@Suppress("LargeClass")
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
    fun `test node's check text lenght`() {
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.elementType == CLASS) {
                Assert.assertTrue(node.checkLength(IntRange(code.length, code.length)))
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test IdentifierName`() {
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        val list = listOf("Test", "foo", "a", "a", "Int", "Int", "a")
        applyToCode(code) { node ->
            node.getIdentifierName()?.let {
                Assert.assertEquals(list[counter], it.text)
                counter++
            }
        }
        Assert.assertEquals(counter, list.size)
    }

    @Test
    fun `test getTypeParameterList`() {
        val code = """
            class Array<T>(val size: Int) {
                
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.getTypeParameterList() != null) {
                Assert.assertEquals("<T>", node.getTypeParameterList()!!.text)
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test getAllIdentifierChildren`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        val list = listOf("Test", "foo", "a", "a", "Int", "Int", "a")
        var counter = 0
        applyToCode(code) { node ->
            node.getAllIdentifierChildren().ifNotEmpty {
                this.forEach { Assert.assertEquals(list[counter], it.text) }
                counter++
            }
        }
        Assert.assertEquals(counter, list.size)
    }

    @Test
    fun `test getAllChildrenWithType`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var firstCounter = 0
        var secondCounter = 0
        applyToCode(code) { node ->
            node.getAllChildrenWithType(CLASS).ifNotEmpty {
                Assert.assertEquals(map { it.text }, listOf(code))
                firstCounter++
            }
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() && node.treeParent.elementType == FILE) {
                Assert.assertEquals(node.getAllChildrenWithType(IDENTIFIER)[0].text, "Test")
                secondCounter++
            }
        }
        Assert.assertEquals(1, firstCounter)
        Assert.assertEquals(1, secondCounter)
    }

    @Test
    fun `test getFirstChildWithType`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() && node.treeParent.elementType == FILE) {
                Assert.assertEquals(node.getFirstChildWithType(IDENTIFIER)!!.text, "Test")
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test hasChildOfType`() {
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.getIdentifierName() != null) {
                Assert.assertTrue(node.hasChildOfType(IDENTIFIER))
                counter++
            }
        }
        Assert.assertEquals(2, counter)
    }

    @Test
    fun `test hasAnyChildOfTypes`() {
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() || node.getAllChildrenWithType(CLASS).isNotEmpty()) {
                Assert.assertTrue(node.hasAnyChildOfTypes(IDENTIFIER, CLASS))
                counter++
            }
        }
        Assert.assertEquals(3, counter)
    }

    @Test
    fun `test findChildBefore`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.findChildBefore(CLASS_BODY, CLASS) != null) {
                Assert.assertEquals(node.findChildBefore(CLASS_BODY, CLASS)!!.text, code)
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test findChildBefore - with siblings`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        val list = listOf("Test", "foo", "a", "a", "Int", "Int", "a")
        applyToCode(code) { node ->
            if (node.findChildBefore(CLASS_BODY, IDENTIFIER) != null) {
                Assert.assertEquals(node.findChildBefore(CLASS_BODY, IDENTIFIER)!!.text, list[counter])
                counter++
            }
        }
        Assert.assertEquals(7, counter)
    }

    @Test
    fun `test findChildAfter`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)?.let {
                Assert.assertEquals("Int", it.text)
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test allSiblings withSelf - true`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code) { node ->
            val setParent = if (node.treeParent != null) {
                node.treeParent.getChildren(null).toSet()
            } else
                setOf(node)
            val setSibling = node.allSiblings(true).toSet()
            Assert.assertEquals(setParent, setSibling)
            Assert.assertTrue(setParent.isNotEmpty())
        }
    }

    @Test
    fun `regression - check for companion object`() {
        var code = """
                object Test {
                    val id = 1
            	}
        """.trimIndent()

        applyToCode(code) { node ->
            if (node.elementType == PROPERTY) {
                Assert.assertFalse(node.isNodeFromCompanionObject())
            }
        }

        code = """
                companion object Test {
                    val id = 1
            	}
        """.trimIndent()

        applyToCode(code) { node ->
            if (node.elementType == PROPERTY) {
                Assert.assertTrue(node.isNodeFromCompanionObject())
            }
        }
    }

    @Test
    fun `test isNodeFromCompanionObject`() {
        var code = """
            class Something{
            	companion object {
                    val id = 1
            	}
            }
        """.trimIndent()
        var firstCounter = 0
        applyToCode(code) { node ->
            if (node.elementType == PROPERTY) {
                Assert.assertTrue(node.isNodeFromCompanionObject())
                firstCounter++
            }
        }
        code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var secondCounter = 0
        applyToCode(code) { node ->
            if (node.elementType == FUN) {
                Assert.assertFalse(node.isNodeFromCompanionObject())
                secondCounter++
            }
        }
        Assert.assertEquals(1, firstCounter)
        Assert.assertEquals(1, secondCounter)
    }

    @Test
    fun `test node is from object `() {
        val code = """
            object Something{
                    val id = 1
            }
        """.trimIndent()
        applyToCode(code) { node ->
            if (node.elementType == PROPERTY) {
                Assert.assertTrue(node.isNodeFromObject())
            }
        }
    }

    @Test
    fun `test isNodeFromFileLevel - node from file level`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.treeParent != null && node.elementType == CLASS) {
                Assert.assertTrue(node.isNodeFromFileLevel())
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `test isNodeFromFileLevel - node isn't from file level`() {
        val code = """
            val x = 2
            
        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.elementType != FILE)
                node.getChildren(null).forEach {
                    Assert.assertFalse(it.isNodeFromFileLevel())
                    counter++
                }
        }
        Assert.assertEquals(8, counter)
    }

    @Test
    fun `test isValProperty`() {
        val code = """
            class Test() {

                private val name = "John"

                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var isVal = false
        applyToCode(code) { node ->
            if (node.isValProperty())
                isVal = true
        }
        Assert.assertTrue(isVal)
    }

    @Test
    fun `test isConst`() {
        val code = """
            class Test() {

                const val SPEED = 10

                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var isConst = false
        applyToCode(code) { node ->
            if (node.isConst())
                isConst = true
        }
        Assert.assertTrue(isConst)
    }

    @Test
    fun `test isVarProperty`() {
        val code = """
            class Test() {

                private var name: String? = null

                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var isVar = false
        applyToCode(code) { node ->
            if (node.isVarProperty())
                isVar = true
        }
        Assert.assertTrue(isVar)
    }

    @Test
    fun `test getAllLLeafsWithSpecificType`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        val list = mutableListOf<ASTNode>()
        val leafWithTypeList = mutableListOf<ASTNode>()
        var firstNode: ASTNode? = null
        applyToCode(code) { node ->
            if (firstNode == null)
                firstNode = node
            if (node.isLeaf() && node.elementType == WHITE_SPACE) {
                leafWithTypeList.add(node)
            }
        }
        firstNode?.getAllLeafsWithSpecificType(WHITE_SPACE, list)
        Assert.assertEquals(list, leafWithTypeList)
    }

    @Test
    fun `test findLeafWithSpecificType`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var firstNode: ASTNode? = null
        var resultNode: ASTNode? = null
        applyToCode(code) { node ->
            if (firstNode == null)
                firstNode = node
            if (resultNode == null && node.elementType == CLASS_BODY) {
                resultNode = node
            }
        }
        firstNode = firstNode?.findLeafWithSpecificType(CLASS_BODY)
        Assert.assertEquals(resultNode!!.text, firstNode!!.text)
    }

    @Test
    fun `test findAllNodesWithSpecificType`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var firstNode: ASTNode? = null
        val listResults = mutableListOf<ASTNode>()
        applyToCode(code) { node ->
            if (firstNode == null)
                firstNode = node
            if (node.elementType == IDENTIFIER) {
                listResults.add(node)
            }
        }
        val listTypes = firstNode?.findAllNodesWithSpecificType(IDENTIFIER)
        Assert.assertEquals(listResults, listTypes)
    }

    @Test
    fun `test findParentNodeWithSpecificType`() {
        val code = """
            val a = ""
            class Test() {
                fun foo() {
                    try {
                    } catch (e: Exception) {
                    }
                }
            }
        """.trimIndent()
        val listResults = mutableListOf<ASTNode>()
        applyToCode(code) { node ->
            if (node.elementType == IDENTIFIER) {
                listResults.add(node)
            }
        }

        listResults.forEach { node ->
            if (node.findParentNodeWithSpecificType(ElementType.CATCH) == null) {
                val identifiers = listOf("Test", "foo", "a")
                Assert.assertTrue("Identifier <${node.text}> expected not to have CATCH parent node", identifiers.contains(node.text))
            } else {
                val identifiers = listOf("e", "Exception")
                Assert.assertTrue("Identifier <${node.text}> expected to have CATCH parent node", identifiers.contains(node.text))
            }
        }
    }

    @Test
    fun `test isAccessibleOutside`() {
        var code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                private fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var firstCounter = 0
        applyToCode(code) { node ->
            if (node.elementType == MODIFIER_LIST) {
                Assert.assertFalse(node.isAccessibleOutside())
                firstCounter++
            }
        }
        code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                public fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        var secondCounter = 0
        applyToCode(code) { node ->
            if (node.elementType == MODIFIER_LIST) {
                Assert.assertTrue(node.isAccessibleOutside())
                secondCounter++
            }
        }
        Assert.assertEquals(1, firstCounter)
        Assert.assertEquals(1, secondCounter)
    }

    @Test
    fun `test leaveOnlyOneNewLine`() {
        val code = """
            var x = 2


        """.trimIndent()
        var counter = 0
        applyToCode(code) { node ->
            if (node.elementType == WHITE_SPACE && node.text.contains("\n\n")) {
                val parent = node.treeParent
                val firstText = node.text
                node.leaveOnlyOneNewLine()
                val secondText = parent.getChildren(null).last().text
                Assert.assertEquals("\n", secondText)
                Assert.assertEquals("\n\n", firstText)
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `moveChildBefore 1 - reverse`() {
        var counter = 0
        applyToCode("""
                |val a = 0
                |val b = 1
            """.trimMargin()) { node ->
            if (node.getChildren(null).isNotEmpty()) {
                val listBeforeMove = node.getChildren(null).map { it.elementType }
                node.getChildren(null).forEachIndexed { index, astNode ->
                    node.moveChildBefore(astNode, node.getChildren(null)[node.getChildren(null).size - index - 1])
                }
                val listAfterMove = node.getChildren(null).map { it.elementType }
                Assert.assertEquals(listBeforeMove, listAfterMove.reversed())
                counter++
            }
        }
        Assert.assertEquals(5, counter)
    }

    @Test
    fun `moveChildBefore 2 - Should correctly move node child to the end`() {
        var counter = 0
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
                counter++
            }
        }
        Assert.assertEquals(1, counter)
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
                Assert.assertFalse(node.isChildAfterAnother(valNode, zero))
                Assert.assertFalse(node.isChildAfterGroup(identifier, listOf(zero, eq)))

                Assert.assertTrue(node.isChildBeforeAnother(identifier, zero))
                Assert.assertTrue(node.isChildBeforeGroup(identifier, listOf(eq, zero)))
                Assert.assertTrue(node.areChildrenBeforeChild(listOf(valNode, identifier, eq), zero))
                Assert.assertTrue(node.areChildrenBeforeGroup(listOf(valNode, identifier), listOf(eq, zero)))

                Assert.assertFalse(node.isChildBeforeAnother(zero, identifier))
                Assert.assertFalse(node.isChildBeforeGroup(zero, listOf(identifier, eq)))
                Assert.assertFalse(node.areChildrenBeforeChild(listOf(identifier, eq, zero), valNode))
                Assert.assertFalse(node.areChildrenBeforeGroup(listOf(eq, zero), listOf(valNode, identifier)))
            }
        }
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
