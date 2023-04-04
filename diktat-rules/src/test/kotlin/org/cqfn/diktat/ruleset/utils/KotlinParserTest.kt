package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.util.applyToCode

import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
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
            |package org.cqfn.diktat.ruleset.utils
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
        Assertions.assertEquals(FILE, node.elementType)
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
            |package org.cqfn.diktat.ruleset.utils
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
            |package org.cqfn.diktat.ruleset.utils
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
            |package org.cqfn.diktat.ruleset.utils
            """.trimMargin()
        val node = KotlinParser().createNode(packageCode, true)
        Assertions.assertEquals(FILE, node.elementType)
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
            |package org.cqfn.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test
            |import org.junit.jupiter.api.Tests
            """.trimMargin()
        val node = KotlinParser().createNode(code, true)
        Assertions.assertEquals(FILE, node.elementType)
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
