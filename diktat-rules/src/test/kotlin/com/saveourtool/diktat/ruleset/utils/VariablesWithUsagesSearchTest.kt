package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithUsages
import com.saveourtool.diktat.util.applyToCode
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class VariablesWithUsagesSearchTest {
    @Test
    fun `testing proper variables search in function`() {
        applyToCode("""
            fun foo(a: Int) {
                fun foo1() {
                    val o = 1
                    val a = 2
                    println(a.o)
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("val o = 1", var1)
                assertEquals(0, vars[var1]?.size)
                assertEquals("val a = 2", var2)
                assertEquals(1, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in function with false positive shadowing`() {
        applyToCode("""
            fun foo() {
                var v = 1
                if (true) {
                    v++
                    var v = 0
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("var v = 1", var1)
                assertEquals(1, vars[var1]?.size)
                assertEquals("var v = 0", var2)
                assertEquals(0, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in function with false positive shadowing and nesting`() {
        applyToCode("""
            fun foo() {
                var v = 1
                if (true) {
                    if (true) {
                        v++
                    }
                    var v = 0
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("var v = 1", var1)
                assertEquals(1, vars[var1]?.size)
                assertEquals("var v = 0", var2)
                assertEquals(0, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in simple class with property`() {
        applyToCode("""
            class A {
                val v = 0
                fun foo() {
                    ++v
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                assertEquals("val v = 0", var1)
                assertEquals(1, vars[var1]?.size)
            }
        }
    }

    @Test
    @Disabled
    // FixMe: very strange behavior of Kotlin
    fun `testing proper variables search in function with a class nested in a function`() {
        applyToCode("""
            fun foo() {
                var a = 0
                class A {
                    var a = 1
                    fun foo() {
                        a++
                    }
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("var a = 0", var1)
                assertEquals(1, vars[var1]?.size)
                assertEquals("var a = 1", var2)
                assertEquals(0, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in class`() {
        applyToCode("""
            class SomeClass {
                val someVal = 0
                fun foo(a: Int) {
                    someVal++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                assertEquals("val someVal = 0", var1)
                assertEquals(1, vars[var1]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in class and global context`() {
        applyToCode("""
            val someVal = 1
            class SomeClass {
                val someVal = 0
                fun foo(a: Int) {
                    someVal++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("val someVal = 1", var1)
                assertEquals(0, vars[var1]?.size)
                assertEquals("val someVal = 0", var2)
                assertEquals(1, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in a nested class that is inside of a function`() {
        applyToCode("""
            fun foo(a: Int) {
                class A {
                    var a = 5
                    fun foo() {
                        println(a)
                    }
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                assertEquals("var a = 5", var1)
                assertEquals(0, vars[var1]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in a nested functions`() {
        applyToCode("""
            fun foo(a: Int) {
                fun foo() {
                    var a = 5
                    println(a)
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                assertEquals("var a = 5", var1)
                assertEquals(1, vars[var1]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search in class with shadowing`() {
        applyToCode("""
            class A {
                var v = 0
                fun foo() {
                    v++
                    var v = 1
                    v++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                assertEquals("var v = 0", var1)
                assertEquals(1, vars[var1]?.size)
                assertEquals("var v = 1", var2)
                assertEquals(1, vars[var2]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search on global level`() {
        applyToCode("""
            var v = 0
            fun foo() {
                v++
                var v = 1
                v++
            }
            class A {
                fun foo() {
                    v++
                    var v = 2
                    v++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                val var3 = keys.elementAt(2)
                assertEquals("var v = 0", var1)
                assertEquals(2, vars[var1]?.size)
                assertEquals("var v = 1", var2)
                assertEquals(1, vars[var2]?.size)
                assertEquals("var v = 2", var3)
                assertEquals(1, vars[var3]?.size)
            }
        }
    }

    @Test
    @Disabled
    fun `testing proper variables search in companion object`() {
        applyToCode("""
            var v = 0
            class A {
                companion object {
                    var v = 1
                }
                fun foo() {
                    v++
                    var v = 2
                    v++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                val var3 = keys.elementAt(1)
                assertEquals("var v = 0", var1)
                assertEquals(0, vars[var1]?.size)
                assertEquals("var v = 1", var2)
                assertEquals(1, vars[var2]?.size)
                assertEquals("var v = 2", var3)
                assertEquals(1, vars[var3]?.size)
            }
        }
    }

    @Test
    @Disabled
    fun `testing proper variables search in companion object with less priority then property`() {
        applyToCode("""
            var v = 0
            class A {
                companion object {
                    var v = 1
                }
                fun foo() {
                    v++
                    var v = 2
                    v++
                }

                var v = 3
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                val var2 = keys.elementAt(1)
                val var3 = keys.elementAt(2)
                assertEquals("var v = 0", var1)
                assertEquals(0, vars[var1]?.size)
                assertEquals("var v = 1", var2)
                assertEquals(0, vars[var2]?.size)
                assertEquals("var v = 2", var3)
                assertEquals(1, vars[var3]?.size)
                assertEquals("var v = 3", var3)
                assertEquals(1, vars[var3]?.size)
            }
        }
    }

    @Test
    fun `testing proper variables search with while statement`() {
        applyToCode("""
            class A {
                fun foo() {
                    var v = 1
                    while(true) {
                        v++
                    }
                    v++
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)
                assertEquals("var v = 1", var1)
                assertEquals(2, vars[var1]?.size)
            }
        }
    }

    @Test
    @Disabled
    fun `testing proper variables search in class with the property in the end`() {
        applyToCode("""
            class A {
                fun foo() {
                    v++
                }

                var v = 1
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == KtFileElementType.INSTANCE) {
                val vars = node.findAllVariablesWithUsages().mapKeys { it.key.text }
                val keys = vars.keys
                val var1 = keys.elementAt(0)

                assertEquals("var v = 1", var1)
                assertEquals(1, vars[var1]?.size)
            }
        }
    }
}
