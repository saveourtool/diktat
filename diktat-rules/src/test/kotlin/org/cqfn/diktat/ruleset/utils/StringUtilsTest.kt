package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.junit.Assert
import org.junit.jupiter.api.Test

class StringUtilsTest {

    @Test
    fun `test simple property`() {
        val node  = StringToNode().createNode("val x: Int = 10")!!
        Assert.assertEquals(PROPERTY, node.elementType)
        Assert.assertEquals("val x: Int = 10", node.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test property with expression`() {
        val node  = StringToNode().createNode("val x = foo(15)")!!
        println(node.prettyPrint())
        Assert.assertEquals(PROPERTY, node.elementType)
        Assert.assertEquals("val x = foo(15)", node.text)
        Assert.assertEquals("foo(15)", node.findChildByType(CALL_EXPRESSION)!!.text)
        Assert.assertEquals(3, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }

    @Test
    fun `test inline function`() {
        val node  = StringToNode().createNode("fun foo(text: String) = text.toUpperCase()")!!
        Assert.assertEquals(FUN, node.elementType)
        Assert.assertEquals("fun foo(text: String) = text.toUpperCase()", node.text)
        Assert.assertEquals("foo", node.getIdentifierName()!!.text)
        Assert.assertEquals(4, node.findAllNodesWithSpecificType(WHITE_SPACE).size)
    }
}