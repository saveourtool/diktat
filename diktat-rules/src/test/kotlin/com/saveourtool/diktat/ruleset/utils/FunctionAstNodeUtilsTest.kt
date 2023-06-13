package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.util.applyToCode

import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class FunctionAstNodeUtilsTest {
    @Test
    fun `should detect parameters in function - no parameters`() {
        applyToCode("fun foo() { }", 1) { node, counter ->
            if (node.elementType == FUN) {
                Assertions.assertFalse(node.hasParameters())
                Assertions.assertTrue(node.parameterNames().isEmpty())
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `should detect parameters in function`() {
        applyToCode("fun foo(a: Int) { }", 1) { node, counter ->
            if (node.elementType == FUN) {
                Assertions.assertTrue(node.hasParameters())
                Assertions.assertEquals(listOf("a"), node.parameterNames())
                counter.incrementAndGet()
            }
        }
    }
}
