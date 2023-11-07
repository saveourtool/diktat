package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.util.applyToCode

import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.lexer.KtTokens.CLASS_KEYWORD
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_DIRECTIVE
import org.jetbrains.kotlin.lexer.KtTokens.IMPORT_KEYWORD
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.KtNodeTypes.PACKAGE_DIRECTIVE
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinParserTest {
    @Test
    fun `test simple property`() {
        val node = KotlinParser().createNode("val x: Int = 10")
        Assertions.assertEquals(PROPERTY, node.elementType)
        Assertions.assertEquals("val x: Int = 10", node.text)
        Assertions.assertEquals(4, node.findAllDescendantsWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    @Suppress("UnsafeCallOnNullableType")
    fun `test oneline function`() {
        val node = KotlinParser().createNode("fun foo(text: String) = text.toUpperCase()")
        Assertions.assertEquals(FUN, node.elementType)
        Assertions.assertEquals("fun foo(text: String) = text.toUpperCase()", node.text)
        Assertions.assertEquals("foo", node.getIdentifierName()!!.text)
        Assertions.assertEquals(4, node.findAllDescendantsWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test invalidate code`() {
        assertThrows<KotlinParseException> { KotlinParser().createNode("simple text") }
        assertThrows<KotlinParseException> { KotlinParser().createNode("") }
        assertThrows<KotlinParseException> { KotlinParser().createNode("fuc fun() = 1") }
    }

    @Test
    fun `test multiline code with import and package`() {
        val code = """
            |package com.saveourtool.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test
            |import org.junit.jupiter.api.Tests
            |
            |class A {
            |   fun foo(){
            |       println("hello")
            |   }
            |}
            """.trimMargin()
        val node = KotlinParser().createNode(code, true)
        Assertions.assertEquals(KtFileElementType.INSTANCE, node.elementType)
        Assertions.assertEquals(PACKAGE_DIRECTIVE, node.firstChildNode.elementType)
    }

    @Test
    fun `test multiline class code`() {
        val code = """
            |class A {
            |   fun foo(){
            |       println("hello")
            |   }
            |}
            """.trimMargin()
        val node = KotlinParser().createNode(code)
        Assertions.assertEquals(CLASS, node.elementType)
        Assertions.assertEquals(CLASS_KEYWORD, node.firstChildNode.elementType)
    }

    @Test
    fun `test multiline class code with import`() {
        val code = """
            |import org.junit.jupiter.api.Test
            |import org.junit.jupiter.api.Tests
            |
            |class A {
            |   fun foo(){
            |       println("hello")
            |   }
            |}
            """.trimMargin()
        assertThrows<KotlinParseException> { KotlinParser().createNode(code) }
    }

    @Test
    @Suppress(
        "UnsafeCallOnNullableType",
        "TOO_LONG_FUNCTION",
        "AVOID_NULL_CHECKS"
    )
    fun `test multiline class code compare with applyToCode`() {
        val emptyClass = """
            |package com.saveourtool.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test
            |
            |class A {
            |}
            """.trimMargin()
        var nodeToApply: ASTNode? = null
        applyToCode(emptyClass, 0) { newNode, _ ->
            if (nodeToApply == null) {
                nodeToApply = newNode
            }
        }
        val resultClass = """
            |package com.saveourtool.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test
            |
            |class A {
            |fun foo() = "Hello"
            |}
            """.trimMargin()
        var resultNode: ASTNode? = null
        applyToCode(resultClass, 0) { newNode, _ ->
            if (resultNode == null) {
                resultNode = newNode
            }
        }
        Assertions.assertTrue(nodeToApply!!.prettyPrint() == KotlinParser().createNode(emptyClass, true).prettyPrint())
        val classNode = nodeToApply!!.findChildByType(CLASS)!!.findChildByType(CLASS_BODY)!!
        val function = """
            |fun foo() = "Hello"
            """.trimMargin()
        classNode.addChild(KotlinParser().createNode(function), classNode.findChildByType(RBRACE))
        classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.findChildByType(RBRACE))
        Assertions.assertTrue(nodeToApply!!.prettyPrint() == resultNode!!.prettyPrint())
    }

    @Test
    fun `check package`() {
        val packageCode = """
            |package com.saveourtool.diktat.ruleset.utils
            """.trimMargin()
        val node = KotlinParser().createNode(packageCode, true)
        Assertions.assertEquals(KtFileElementType.INSTANCE, node.elementType)
        Assertions.assertEquals(packageCode, node.text)
        Assertions.assertEquals(PACKAGE_DIRECTIVE, node.firstChildNode.elementType)
    }

    @Test
    fun `check import`() {
        val importCode = """
            |import org.junit.jupiter.api.Test
            """.trimMargin()
        val node = KotlinParser().createNode(importCode)
        Assertions.assertEquals(IMPORT_DIRECTIVE, node.elementType)
        Assertions.assertEquals(importCode, node.text)
        Assertions.assertEquals(IMPORT_KEYWORD, node.firstChildNode.elementType)
    }

    @Test
    fun `check imports`() {
        val importCode = """
            |import org.junit.jupiter.api.Test
            |import org.junit.jupiter.api.Tests
            |import org.junit.jupiter.api
            """.trimMargin()
        val node = KotlinParser().createNode(importCode)
        Assertions.assertEquals(IMPORT_LIST, node.elementType)
        Assertions.assertEquals(importCode, node.text)
        Assertions.assertEquals(IMPORT_DIRECTIVE, node.firstChildNode.elementType)
    }

    @Test
    fun `check package and import`() {
        val code = """
            |package com.saveourtool.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test
            |import org.junit.jupiter.api.Tests
            """.trimMargin()
        val node = KotlinParser().createNode(code, true)
        Assertions.assertEquals(KtFileElementType.INSTANCE, node.elementType)
        Assertions.assertEquals(code, node.text)
        Assertions.assertEquals(PACKAGE_DIRECTIVE, node.firstChildNode.elementType)
    }

    @Test
    @Suppress("UnsafeCallOnNullableType")
    fun `check KDoc`() {
        val code = """
            |/**
            |* [link]haha
            |*/
            |fun foo()
            """.trimMargin()
        val node = KotlinParser().createNode(code)
        Assertions.assertEquals(KDOC, node.findChildByType(FUN)!!.firstChildNode.elementType)

        val kdocText = """
            /**
            * [link]haha
            */
        """.trimIndent()
        Assertions.assertEquals(kdocText, node.findChildByType(FUN)!!.firstChildNode.text)
    }

    @Test
    fun `test createNodeForInit`() {
        val code = """
            |init {
            |   println("A")
            |   // import is a weak keyword
            |   println("B")
            |}
            """.trimMargin()
        val node = KotlinParser().createNodeForInit(code)
        Assertions.assertEquals(CALL_EXPRESSION, node.elementType)
        Assertions.assertEquals(code, node.text)
    }

    @Test
    fun `test createNodeForSecondaryConstructor`() {
        val code = """
            |constructor(a: Int) {
            |   // import is a weak keyword
            |   b = a.toString()
            |}
            """.trimMargin()
        val node = KotlinParser().createNodeForSecondaryConstructor(code)
        Assertions.assertEquals(SECONDARY_CONSTRUCTOR, node.elementType)
        Assertions.assertEquals(code, node.text)
    }
}
