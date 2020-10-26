package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.ruleset.utils.search.isShadowOf
import org.cqfn.diktat.util.applyToCode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class IsShadowOfTest {
    @Test
    fun `testing isShadowOf simple case`() {
        applyToCode("""
            fun foo() {
                val b = 0 // properties[0]
                val a = b // properties[1]
                if (true) {
                    val a = 1 // properties[2]
                }
            }
        """.trimIndent(), 3) { node, _ ->
            if (node.elementType == FILE) {
                val properties = node.findAllNodesWithSpecificType(PROPERTY).map { it.findChildByType(IDENTIFIER) }
                assertTrue(properties[2]!!.isShadowOf(properties[1]!!))
                assertFalse(properties[1]!!.isShadowOf(properties[2]!!))
                assertFalse(properties[2]!!.isShadowOf(properties[0]!!))
            }
        }
    }

    @Test
    fun `testing isShadowOf with function arguments`() {
        applyToCode("""
            fun foo(a: Int) { // parameters[0]
                val a = b // properties[0]
                if (true) {
                    val a = 1 // properties[1]
                }
            }
        """.trimIndent(), 1) { node, _ ->
            if (node.elementType == FILE) {
                val parameters = node.findAllNodesWithSpecificType(VALUE_PARAMETER).map { it.findChildByType(IDENTIFIER) }
                val properties = node.findAllNodesWithSpecificType(PROPERTY).map { it.findChildByType(IDENTIFIER) }
                assertTrue(properties[1]!!.isShadowOf(parameters[0]!!))
            }
        }
    }

    @Test
    fun `testing isShadowOf with lambda arguments`() {
        applyToCode("""
            fun foo(a: List<String>) { // parameters[0]
                a.forEach {
                    a -> // parameters[1]
                        val a = 5 //properties[0]
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == FILE) {
                val parameters = node.findAllNodesWithSpecificType(VALUE_PARAMETER).map { it.findChildByType(IDENTIFIER) }
                val properties = node.findAllNodesWithSpecificType(PROPERTY).map { it.findChildByType(IDENTIFIER) }
                assertTrue(properties[0]!!.isShadowOf(parameters[0]!!))
                assertTrue(parameters[1]!!.isShadowOf(parameters[0]!!))
                assertTrue(properties[0]!!.isShadowOf(parameters[1]!!))
                assertFalse(parameters[0]!!.isShadowOf(properties[0]!!))
            }
        }
    }

    @Test
    fun `testing isShadowOf with nested functions`() {
        applyToCode("""
            fun foo(a: Int) { // parameters[0]
                fun foo1(a: Int) { // parameters[1]
                    println(a)
                    val a = 1 // properties[0]
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == FILE) {
                val parameters = node.findAllNodesWithSpecificType(VALUE_PARAMETER).map { it.findChildByType(IDENTIFIER) }
                val properties = node.findAllNodesWithSpecificType(PROPERTY).map { it.findChildByType(IDENTIFIER) }
                assertTrue(parameters[1]!!.isShadowOf(parameters[0]!!))
                assertTrue(parameters[1]!!.isShadowOf(parameters[0]!!))
            }
        }
    }
}


