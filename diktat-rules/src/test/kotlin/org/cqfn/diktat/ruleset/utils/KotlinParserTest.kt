package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinParserTest {

    @Test
    fun `test simple property`() {
        val node = KotlinParser().createNode("val x: Int = 10")
        Assert.assertEquals(PROPERTY, node.elementType)
        Assert.assertEquals("val x: Int = 10", node.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test oneliner function`() {
        val node = KotlinParser().createNode("fun foo(text: String) = text.toUpperCase()")
        Assert.assertEquals(FUN, node.elementType)
        Assert.assertEquals("fun foo(text: String) = text.toUpperCase()", node.text)
        Assert.assertEquals("foo", node.getIdentifierName()!!.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test invalidate code`() {
        assertThrows<KotlinParseException> { KotlinParser().createNode("simple text") }
        assertThrows<KotlinParseException> { KotlinParser().createNode("fuc fun() = 1") }
    }

    @Test
    fun `test multiline code with import and package`() {
        val code = """
            |package org.cqfn.diktat.ruleset.utils
            |
            |import org.junit.jupiter.api.Test 
            |
            |class A {
            |   fun foo(){
            |       println("hello")
            |   }
            |}
            """.trimMargin()
        val node = KotlinParser().createNode(code)
        Assert.assertEquals(FILE, node.elementType)
        Assert.assertEquals(PACKAGE_DIRECTIVE, node.firstChildNode.elementType)
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
        Assert.assertEquals(CLASS, node.elementType)
        Assert.assertEquals(CLASS_KEYWORD, node.firstChildNode.elementType)
    }
}
