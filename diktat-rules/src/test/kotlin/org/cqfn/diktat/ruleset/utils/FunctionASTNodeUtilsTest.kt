package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FUN
import org.assertj.core.api.Assertions
import org.cqfn.diktat.util.applyToCode
import org.junit.Assert
import org.junit.jupiter.api.Test

class FunctionASTNodeUtilsTest {
    @Test
    fun `should detect parameters in function - no parameters`() {
        var counter = 0
        applyToCode("fun foo() { }") {
            if (it.elementType == FUN) {
                Assert.assertFalse(it.hasParameters())
                Assertions.assertThat(it.parameterNames()).isEmpty()
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }

    @Test
    fun `should detect parameters in function`() {
        var counter = 0
        applyToCode("fun foo(a: Int) { }") {
            if (it.elementType == FUN) {
                Assert.assertTrue(it.hasParameters())
                Assert.assertEquals(listOf("a"), it.parameterNames())
                counter++
            }
        }
        Assert.assertEquals(1, counter)
    }
}
