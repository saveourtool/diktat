package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
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
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.ElementType
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.psiUtil.parents
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
    fun `test node's check text lenght`(){
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.elementType == ElementType.CLASS)
                Assert.assertTrue(node.checkLength(IntRange(code.length, code.length)))
        }
    }

    @Test
    fun `test IdentifierName`(){
        val code = """
            class Test {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.getIdentifierName() != null)
                Assert.assertTrue(listOf("Test", "foo", "a", "Int").contains(node.getIdentifierName()!!.text))
        }
    }

    @Test
    fun `test getTypeParameterList`(){
        val code = """
            class Test(val idу: Int) {
                private val id: Int = 10
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.getTypeParameterList() != null) {
                Assert.assertTrue(listOf("Test", "foo", "a", "Int").contains(node.getTypeParameterList()!!.text))
            }
        }
    }

    @Test
    fun `test getAllIdentifierChildren`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        val list = mutableListOf<String>()
        applyToCode(code){node ->
            if (node.getAllIdentifierChildren().isNotEmpty())
                node.getAllIdentifierChildren().forEach { list.add(it.text) }
        }
        Assert.assertTrue(list.containsAll(listOf("Test", "foo", "a", "Int")))
    }

    @Test
    fun `test getAllChildrenWithType`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            val list = mutableListOf<String>()
            if (node.getAllChildrenWithType(CLASS).isNotEmpty()) {
                node.getAllChildrenWithType(CLASS).forEach {
                    list.add(it.text)
                }
                Assert.assertEquals(list, listOf(code))
            }
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty()) {
                Assert.assertEquals(node.getAllChildrenWithType(IDENTIFIER)[0].text, node.getAllIdentifierChildren()[0].text)
                Assert.assertEquals(node.getAllChildrenWithType(IDENTIFIER)[0].text, node.getIdentifierName()!!.text)
            }
        }
    }

    @Test
    fun `test getFirstChildWithType`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if(node.getAllChildrenWithType(IDENTIFIER).isNotEmpty()){
                Assert.assertEquals(node.getFirstChildWithType(IDENTIFIER)!!.text, node.getAllChildrenWithType(IDENTIFIER)[0].text)
                Assert.assertEquals(node.getFirstChildWithType(IDENTIFIER)!!.text, node.getIdentifierName()!!.text)
            }
        }
    }

    @Test
    fun `test hasChildOfType`(){
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.getIdentifierName() != null)
                Assert.assertTrue(node.hasChildOfType(IDENTIFIER))
        }
    }

    @Test
    fun `test hasAnyChildOfTypes`(){
        val code = """
            class Test {
                val x = 0
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.getAllChildrenWithType(IDENTIFIER).isNotEmpty() || node.getAllChildrenWithType(CLASS).isNotEmpty()) {
                val param = arrayOf(IDENTIFIER, CLASS)
                Assert.assertTrue(node.hasAnyChildOfTypes(*param))
            }
        }
    }

    @Test
    fun `test findChildBefore`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.findChildBefore(CLASS_BODY, CLASS) != null)
                Assert.assertEquals(node.findChildBefore(CLASS_BODY, CLASS)!!.text, code)
        }
    }

    @Test
    fun `test findChildAfter`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE) != null)
                Assert.assertEquals(node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)!!.text, "Int")
        }
    }

    @Test
    fun `test allSiblings`(){
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            val list = mutableListOf<IElementType>()
            node.allSiblings(true).forEach {
                list.add(it.elementType)
            }
            Assert.assertTrue(list.contains(node.elementType))
        }
    }

    @Test
    fun `test isNodeFromCompanionObject`() {
        var code = """
            object DataProviderManager {
                fun registerDataProvider(provider: DataProvider) {}

                val allDataProviders: Collection<DataProvider>
            }
        """.trimIndent()
        applyToCode(code){node ->
            if(node.elementType == FUN)
                Assert.assertTrue(node.isNodeFromCompanionObject())
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
        applyToCode(code){node ->
            if(node.elementType == FUN)
                Assert.assertFalse(node.isNodeFromCompanionObject())
        }
    }

    @Test
    fun `test isNodeFromFileLevel`() {
        val code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if(node.treeParent != null && node.elementType == CLASS) {
                Assert.assertTrue(node.isNodeFromFileLevel())
            }
        }
    }

    @Test
    fun `test isValProperty`(){
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
    fun `test isConst`(){
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
    fun `test isVarProperty`(){
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
    fun `test getAllLLeafsWithSpecificType`(){
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
        var leafWithTypeCount = 0
        var firstNode: ASTNode? = null
        applyToCode(code){node ->
            if(firstNode == null)
                firstNode = node
            if (node.isLeaf() && node.elementType == WHITE_SPACE) {
                leafWithTypeCount++
            }
        }
        firstNode?.getAllLLeafsWithSpecificType(WHITE_SPACE, list)
        Assert.assertEquals(list.size, leafWithTypeCount)
    }

    @Test
    fun `test findLeafWithSpecificType`(){
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
        applyToCode(code){node ->
            if(firstNode == null)
                firstNode = node
            if (resultNode == null && node.elementType == CLASS_BODY) {
                resultNode = node
            }
        }
        firstNode = firstNode?.findLeafWithSpecificType(CLASS_BODY)
        Assert.assertEquals(resultNode?.text, firstNode?.text)
    }

    @Test
    fun `test findAllNodesWithSpecificType`(){
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
        var listResults = mutableListOf<ASTNode>()
        applyToCode(code){node ->
            if(firstNode == null)
                firstNode = node
            if (node.elementType == IDENTIFIER) {
                listResults.add(node)
            }
        }
        val listTypes = firstNode?.findAllNodesWithSpecificType(IDENTIFIER)
        Assert.assertEquals(listResults, listTypes)
    }


    @Test
    fun `test isAccessibleOutside`(){
        var code = """
            class Test() {
                /**
                * test method
                * @param a - dummy int
                */
                private fun foo(a: Int): Int = 2 * a
            }
        """.trimIndent()
        applyToCode(code){node ->
            if (node.elementType == MODIFIER_LIST)
                Assert.assertFalse(node.isAccessibleOutside())
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
        applyToCode(code){node ->
            if (node.elementType == MODIFIER_LIST)
                Assert.assertTrue(node.isAccessibleOutside())
        }
    }
    @Test
    fun `test leaveOnlyOneNewLine`() {
        val code = """
            var x = 2
            
            
        """.trimIndent()
        applyToCode(code) { node ->
            if (node.elementType == WHITE_SPACE && node.text.contains("\n\n")) {
                val parent = node.treeParent
                val firstText = node.text
                node.leaveOnlyOneNewLine()
                var secondText: String? = null
                parent.getChildren(null).forEach { newNode -> secondText = newNode.text }
                Assert.assertNotEquals(firstText, secondText)
            }
        }
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
