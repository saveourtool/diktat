package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinParserTest {

    @Test
    fun `test simple property`() {
        val node  = KotlinParser().createNode("val x: Int = 10")
        Assert.assertEquals(PROPERTY, node.elementType)
        Assert.assertEquals("val x: Int = 10", node.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test oneliner function`() {
        val node  = KotlinParser().createNode("fun foo(text: String) = text.toUpperCase()")
        Assert.assertEquals(FUN, node.elementType)
        Assert.assertEquals("fun foo(text: String) = text.toUpperCase()", node.text)
        Assert.assertEquals("foo", node.getIdentifierName()!!.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test invalidate code`() {
        assertThrows<KotlinParseException> {KotlinParser().createNode("simple text")}
        assertThrows<KotlinParseException> {KotlinParser().createNode("fuc fun() = 1")}
    }

    @Test
    fun `test multiline code`(){
        val code = """
            |import org.junit.jupiter.api.Test 
            |class A {
            |   fun foo(){
            |       println("hello")
            |   }
            |}
            """.trimMargin()
        val node  = KotlinParser().createNode(code)
        println(node.prettyPrint())
    }
}