package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FUN
import org.assertj.core.api.Assertions
import org.cqfn.diktat.util.applyToCode
import org.junit.Assert
import org.junit.Test

class FunctionASTNodeUtilsTest {
    @Test
    fun `should detect parameters in function - no parameters`() {
        applyToCode("fun foo() { }", 1) { node, counter ->
            if (node.elementType == FUN) {
                Assert.assertFalse(node.hasParameters())
                Assertions.assertThat(node.parameterNames()).isEmpty()
                counter.incrementAndGet()
            }
        }
    }

    @Test
    fun `should detect parameters in function`() {
        applyToCode("fun foo(a: Int) { }", 1) { node, counter ->
            if (node.elementType == FUN) {
                Assert.assertTrue(node.hasParameters())
                Assert.assertEquals(listOf("a"), node.parameterNames())
                counter.incrementAndGet()
            }
        }
    }
}
