@file:Suppress(
    "HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE",
    "LOCAL_VARIABLE_EARLY_DECLARATION",
    "AVOID_NULL_CHECKS",
)

package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.util.applyToCode

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
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
import com.pinterest.ktlint.core.ast.nextCodeSibling
import com.pinterest.ktlint.core.ast.nextSibling
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("LargeClass", "UnsafeCallOnNullableType")
class AstNodeUtilsTest {
    @Test
    @Suppress("TOO_LONG_FUNCTION")
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
    fun `test node's check text length`() {
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code, 1) { node, counter ->
            if (node.elementType == CLASS) {
                Assertions.assertTrue(node.isTextLengthInRange(IntRange(code.length, code.length)))
                counter.incrementAndGet()
            }
        }
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
        val list = listOf("Test", "foo", "a", "a", "Int", "Int", "a")
        applyToCode(code, 7) { node, counter ->
            node.getIdentifierName()?.let {
                Assertions.assertEquals(list[counter.get()], it.text)
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test getTypeParameterList`() {
        val code = """
            class Array<T>(val size: Int) {

            }
        """.trimIndent()
        applyToCode(code, 1) { node, counter ->
            if (node.getTypeParameterList() != null) {
                Assertions.assertEquals("<T>", node.getTypeParameterList()!!.text)
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 7) { node, counter ->
            node.getAllChildrenWithType(IDENTIFIER).ifNotEmpty {
                this.forEach { Assertions.assertEquals(list[counter.get()], it.text) }
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 2) { node, counter ->
            node.getAllChildrenWithType(CLASS).ifNotEmpty {
                Assertions.assertEquals(map { it.text }, listOf(code))
                counter.incrementAndGet()
            }
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() && node.treeParent.elementType == FILE) {
                Assertions.assertEquals(node.getAllChildrenWithType(IDENTIFIER)[0].text, "Test")
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 1) { node, counter ->
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() && node.treeParent.elementType == FILE) {
                Assertions.assertEquals(node.getFirstChildWithType(IDENTIFIER)!!.text, "Test")
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test hasChildOfType`() {
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        applyToCode(code, 2) { node, counter ->
            if (node.getIdentifierName() != null) {
                Assertions.assertTrue(node.hasChildOfType(IDENTIFIER))
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test hasAnyChildOfTypes`() {
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        applyToCode(code, 3) { node, counter ->
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() || node.getAllChildrenWithType(CLASS).isNotEmpty()) {
                Assertions.assertTrue(node.hasAnyChildOfTypes(IDENTIFIER, CLASS))
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 1) { node, counter ->
            if (node.findChildBefore(CLASS_BODY, CLASS) != null) {
                Assertions.assertEquals(node.findChildBefore(CLASS_BODY, CLASS)!!.text, code)
                counter.incrementAndGet()
            }
        }
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
        val list = listOf("Test", "foo", "a", "a", "Int", "Int", "a")
        applyToCode(code, 7) { node, counter ->
            if (node.findChildBefore(CLASS_BODY, IDENTIFIER) != null) {
                Assertions.assertEquals(node.findChildBefore(CLASS_BODY, IDENTIFIER)!!.text, list[counter.get()])
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 1) { node, counter ->
            node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)?.let {
                Assertions.assertEquals("Int", it.text)
                counter.incrementAndGet()
            }
        }
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
        applyToCode(code, 0) { node, _ ->
            val setParent = if (node.treeParent != null) {
                node.treeParent.getChildren(null).toSet()
            } else {
                setOf(node)
            }
            val setSibling = node.allSiblings(true).toSet()
            Assertions.assertEquals(setParent, setSibling)
            Assertions.assertTrue(setParent.isNotEmpty())
        }
    }

    @Test
    fun `regression - check for companion object`() {
        applyToCode("""
                object Test {
                    val id = 1
            	}
            """.trimIndent(), 1) { node, counter ->
            if (node.elementType == PROPERTY) {
                Assertions.assertFalse(node.isNodeFromCompanionObject())
                counter.incrementAndGet()
            }
        }

        applyToCode("""
                companion object Test {
                    val id = 1
            	}
            """.trimIndent(), 1) { node, counter ->
            if (node.elementType == PROPERTY) {
                Assertions.assertTrue(node.isNodeFromCompanionObject())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test isNodeFromCompanionObject`() {
        val positiveExample = """
            class Something{
            	companion object {
                    val id = 1
            	}
            }
        """.trimIndent()
        applyToCode(positiveExample, 1) { node, counter ->
            if (node.elementType == PROPERTY) {
                Assertions.assertTrue(node.isNodeFromCompanionObject())
                counter.incrementAndGet()
            }
        }
        val negativeExample = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(negativeExample, 1) { node, counter ->
            if (node.elementType == FUN) {
                Assertions.assertFalse(node.isNodeFromCompanionObject())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test node is from object `() {
        val code = """
            object Something{
                    val id = 1
            }
        """.trimIndent()
        applyToCode(code, 1) { node, counter ->
            if (node.elementType == PROPERTY) {
                Assertions.assertTrue(node.isNodeFromObject())
                counter.incrementAndGet()
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
        applyToCode(code, 1) { node, counter ->
            if (node.treeParent != null && node.elementType == CLASS) {
                Assertions.assertTrue(node.isNodeFromFileLevel())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test isNodeFromFileLevel - node isn't from file level`() {
        val code = """
            val x = 2

        """.trimIndent()
        applyToCode(code, 8) { node, counter ->
            if (node.elementType != FILE) {
                node.getChildren(null).forEach {
                    Assertions.assertFalse(it.isNodeFromFileLevel())
                    counter.incrementAndGet()
                }
            }
        }
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
        applyToCode(code, 0) { node, _ ->
            if (node.isValProperty()) {
                isVal = true
            }
        }
        Assertions.assertTrue(isVal)
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
        applyToCode(code, 0) { node, _ ->
            if (node.isConst()) {
                isConst = true
            }
        }
        Assertions.assertTrue(isConst)
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
        applyToCode(code, 0) { node, _ ->
            if (node.isVarProperty()) {
                isVar = true
            }
        }
        Assertions.assertTrue(isVar)
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
        val list: MutableList<ASTNode> = mutableListOf()
        val leafWithTypeList: MutableList<ASTNode> = mutableListOf()
        var firstNode: ASTNode? = null
        applyToCode(code, 0) { node, _ ->
            if (firstNode == null) {
                firstNode = node
            }
            if (node.isLeaf() && node.elementType == WHITE_SPACE) {
                leafWithTypeList.add(node)
            }
        }
        firstNode?.getAllLeafsWithSpecificType(WHITE_SPACE, list)
        Assertions.assertEquals(list, leafWithTypeList)
    }

    @Test
    @Suppress("UnsafeCallOnNullableType")
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
        applyToCode(code, 0) { node, _ ->
            if (firstNode == null) {
                firstNode = node
            }
            if (resultNode == null && node.elementType == CLASS_BODY) {
                resultNode = node
            }
        }
        firstNode = firstNode?.findLeafWithSpecificType(CLASS_BODY)
        Assertions.assertEquals(resultNode!!.text, firstNode!!.text)
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
        val listResults: MutableList<ASTNode> = mutableListOf()
        applyToCode(code, 0) { node, _ ->
            if (firstNode == null) {
                firstNode = node
            }
            if (node.elementType == IDENTIFIER) {
                listResults.add(node)
            }
        }
        val listTypes = firstNode?.findAllDescendantsWithSpecificType(IDENTIFIER)
        Assertions.assertEquals(listResults, listTypes)
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
        val listResults: MutableList<ASTNode> = mutableListOf()
        applyToCode(code, 0) { node, _ ->
            if (node.elementType == IDENTIFIER) {
                listResults.add(node)
            }
        }

        listResults.forEach { node ->
            if (node.findParentNodeWithSpecificType(ElementType.CATCH) == null) {
                val identifiers = listOf("Test", "foo", "a")
                Assertions.assertTrue(identifiers.contains(node.text)) { "Identifier <${node.text}> expected not to have CATCH parent node" }
            } else {
                val identifiers = listOf("e", "Exception")
                Assertions.assertTrue(identifiers.contains(node.text)) { "Identifier <${node.text}> expected to have CATCH parent node" }
            }
        }
    }

    @Test
    fun `test isAccessibleOutside`() {
        val negativeExample = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                private fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(negativeExample, 1) { node, counter ->
            if (node.elementType == MODIFIER_LIST) {
                Assertions.assertFalse(node.isAccessibleOutside())
                counter.incrementAndGet()
            }
        }
        val positiveExample = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                public fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(positiveExample, 1) { node, counter ->
            if (node.elementType == MODIFIER_LIST) {
                Assertions.assertTrue(node.isAccessibleOutside())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test leaveOnlyOneNewLine`() {
        val code = """
            var x = 2


        """.trimIndent()
        applyToCode(code, 1) { node, counter ->
            if (node.elementType == WHITE_SPACE && node.text.contains("\n\n")) {
                val parent = node.treeParent
                val firstText = node.text
                node.leaveOnlyOneNewLine()
                val secondText = parent
                    .getChildren(null)
                    .last()
                    .text
                Assertions.assertEquals("\n", secondText)
                Assertions.assertEquals("\n\n", firstText)
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `moveChildBefore 1 - reverse`() {
        applyToCode("""
                |val a = 0
                |val b = 1
            """.trimMargin(), 5) { node, counter ->
            if (node.getChildren(null).isNotEmpty()) {
                val listBeforeMove = node.getChildren(null).map { it.elementType }
                node.getChildren(null).forEachIndexed { index, astNode ->
                    node.moveChildBefore(astNode, node.getChildren(null)[node.getChildren(null).size - index - 1])
                }
                val listAfterMove = node.getChildren(null).map { it.elementType }
                Assertions.assertEquals(listBeforeMove, listAfterMove.reversed())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `moveChildBefore 2 - Should correctly move node child to the end`() {
        applyToCode("""
                |val a = 0
                |val b = 1""".trimMargin(), 1) { node, counter ->
            if (node.elementType == FILE) {
                val val1 = node.getFirstChildWithType(PROPERTY)!!
                val val2 = val1.nextSibling { it.elementType == PROPERTY }!!
                node.moveChildBefore(val2, val1, true)
                node.addChild(PsiWhiteSpaceImpl("\n"), val1)
                Assertions.assertTrue(node.text == """
                    |val b = 1
                    |val a = 0
                    |
                    """.trimMargin()
                )
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `isChildAfterGroup test`() {
        applyToCode("val x = 0", 1) { node, counter ->
            if (node.elementType == PROPERTY) {
                val valNode = node.getFirstChildWithType(VAL_KEYWORD)!!
                val identifier = node.getFirstChildWithType(IDENTIFIER)!!
                val eq = node.getFirstChildWithType(EQ)!!
                val zero = node.getFirstChildWithType(INTEGER_CONSTANT)!!

                Assertions.assertTrue(node.isChildAfterAnother(zero, valNode))
                Assertions.assertTrue(node.isChildAfterGroup(zero, listOf(identifier, eq)))
                Assertions.assertFalse(node.isChildAfterAnother(valNode, zero))
                Assertions.assertFalse(node.isChildAfterGroup(identifier, listOf(zero, eq)))

                Assertions.assertTrue(node.isChildBeforeAnother(identifier, zero))
                Assertions.assertTrue(node.isChildBeforeGroup(identifier, listOf(eq, zero)))
                Assertions.assertTrue(node.areChildrenBeforeChild(listOf(valNode, identifier, eq), zero))
                Assertions.assertTrue(node.areChildrenBeforeGroup(listOf(valNode, identifier), listOf(eq, zero)))

                Assertions.assertFalse(node.isChildBeforeAnother(zero, identifier))
                Assertions.assertFalse(node.isChildBeforeGroup(zero, listOf(identifier, eq)))
                Assertions.assertFalse(node.areChildrenBeforeChild(listOf(identifier, eq, zero), valNode))
                Assertions.assertFalse(node.areChildrenBeforeGroup(listOf(eq, zero), listOf(valNode, identifier)))

                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `test line of text extraction`() {
        applyToCode("""
            class Example {
                fun
                    foo() { }
            }
        """.trimIndent(), 1) { node, counter ->
            if (node.elementType == IDENTIFIER && node.text == "foo") {
                Assertions.assertEquals("foo() { }", node.extractLineOfText())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    @Suppress("TOO_LONG_FUNCTION", "TOO_MANY_LINES_IN_LAMBDA")
    fun `test lambda contains it`() {
        applyToCode("""
            |fun bar(lambda: (s: String) -> Unit) {
            |   lambda("bar")
            |}
            |
            |fun foo(lambda: (s: String) -> Unit) {
            |   lambda("foo")
            |}
            |
            |fun test() {
            |   // test1
            |   foo { f1 ->
            |       bar { b1 ->
            |           println(f1 + " -> " + b1)
            |       }
            |   }
            |   // test2
            |   foo {
            |       bar { b2 ->
            |           println(it + " -> " + b2)
            |       }
            |   }
            |   // test3
            |   foo { f3 ->
            |       bar {
            |           println(f3 + " -> " + it)
            |       }
            |   }
            |}
        """.trimMargin(), 3) { node, counter ->
            if (node.elementType == EOL_COMMENT) {
                node.nextCodeSibling()
                    ?.lastChildNode
                    ?.firstChildNode
                    ?.let {
                        when (node.text) {
                            "// test1" -> Assertions.assertFalse(doesLambdaContainIt(it))
                            "// test2" -> Assertions.assertTrue(doesLambdaContainIt(it))
                            "// test3" -> Assertions.assertFalse(doesLambdaContainIt(it))
                            else -> {
                                // this is a generated else block
                            }
                        }
                        counter.incrementAndGet()
                    }
            }
        }
    }
}

private class PrettyPrintingVisitor(private val elementType: IElementType,
                                    private val level: Int,
                                    private val maxLevel: Int,
                                    private val expected: String
) : Rule("test:print-ast") {
    override fun beforeVisitChildNodes(node: ASTNode,
                                       autoCorrect: Boolean,
                                       emit: EmitType
    ) {
        if (node.elementType == elementType) {
            Assertions.assertEquals(
                expected.replace("\n", System.lineSeparator()),
                node.prettyPrint(level, maxLevel)
            )
        }
    }

    companion object {
        fun assertStringRepr(
            elementType: IElementType,
            @Language("kotlin") code: String,
            level: Int = 0,
            maxLevel: Int = -1,
            expected: String
        ) {
            KtLint.lint(
                KtLint.ExperimentalParams(
                    text = code,
                    ruleProviders = setOf(RuleProvider {
                        PrettyPrintingVisitor(elementType, level, maxLevel, expected)
                    }),
                    cb = { _, _ -> }
                )
            )
        }
    }
}
