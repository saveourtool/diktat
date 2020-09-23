package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FILE
import org.cqfn.diktat.util.applyToCode
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class VariableSearchASTUtilsTest {
    @Test
    fun `testing proper variables search`() {
        applyToCode("""
            val a = "myVal"
            fun foo(a: Int) {
                fun foo1() {
                    val a = "other"
                    println(a)
                }
            }
        """.trimIndent(), 0) {
            node, counter ->
            if (node.elementType == FILE) {
                val a = node.collectAllVariablesWithUsagesInFile()
                a.forEach { (key, value) -> println(">>> RES " + key) }
            }
        }
    }
}

