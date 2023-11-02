package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithAssignments
import com.saveourtool.diktat.util.applyToCode
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class VariablesWithAssignmentsSearchTest {
    @Test
    fun `testing proper variables search in function`() {
        applyToCode("""
            fun foo(a: Int) {
                fun foo1() {
                    var o = 1
                    b = o
                    c = o
                    o = 15
                    o = 17
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithAssignments().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                Assertions.assertEquals("var o = 1", var1)
                Assertions.assertEquals(2, vars[var1]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in class`() {
        applyToCode("""
            class A {
                var o = 1
                fun foo(a: Int) {
                    fun foo1() {
                        b = o
                        c = o
                        d = o
                        o = 15
                        o = 17
                    }
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithAssignments().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                Assertions.assertEquals("var o = 1", var1)
                Assertions.assertEquals(2, vars[var1]?.size)
            }
        }
    }

    @Test
    @Disabled
    fun `testing proper variables search with lambda`() {
        applyToCode("""
                fun foo(a: Int) {
                    var a = 1
                    a++
                }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithAssignments().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                Assertions.assertEquals("var a = 1", var1)
                Assertions.assertEquals(1, vars[var1]?.size)
            }
        }
    }
}
