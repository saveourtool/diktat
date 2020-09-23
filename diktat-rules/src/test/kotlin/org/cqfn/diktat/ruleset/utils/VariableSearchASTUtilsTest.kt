package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FILE
import org.cqfn.diktat.util.applyToCode
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class VariableSearchASTUtilsTest {
    @Test
    fun `testing proper variables search in function`() {
        applyToCode("""
            fun foo(a: Int) {
                fun foo1() {
                    val o = "1"
                    val a = "other"
                    println(a.o)
                }
            }
        """.trimIndent(), 0) {
            node, counter ->
            if (node.elementType == FILE) {
                val a = node.collectAllDeclaredVariablesWithUsages()
                a.forEach { (key, value) -> println(">>> RES " + key.text + value.map{it.text}.joinToString(",")) }
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
        """.trimIndent(), 0) {
            node, counter ->
            if (node.elementType == FILE) {
                val a = node.collectAllDeclaredVariablesWithUsages()
                println(a)
                a.forEach { (key, value) -> println(">>> RES " + key.text + value.map{it.text}.joinToString(",")) }
            }
        }
    }
}

