package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.ruleset.utils.search.VariableSearch
import org.cqfn.diktat.ruleset.utils.search.findAllVariablesWithAssignments
import org.cqfn.diktat.util.applyToCode
import org.jetbrains.kotlin.psi.KtProperty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@Suppress("UnsafeCallOnNullableType")
class VariablesSearchTest {
    @Test
    fun `testing requirement for collecting variables`() {
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
        """.trimIndent(), 0) { node, counter ->
            if (node.elementType != ElementType.FILE) {
                val thrown = Assertions.assertThrows(IllegalArgumentException::class.java) {
                    val variablesSearchAbstract: VariableSearch = Mockito.mock(VariableSearch::class.java, Mockito.CALLS_REAL_METHODS)
                    val nodeField = VariableSearch::class.java.getDeclaredField("node")
                    val filter = VariableSearch::class.java.getDeclaredField("filterForVariables")
                    nodeField.isAccessible = true
                    filter.isAccessible = true

                    nodeField.set(variablesSearchAbstract, node)
                    filter.set(variablesSearchAbstract, ::filter)

                    variablesSearchAbstract.collectVariables()
                }
                assertTrue(thrown.message!!.contains("To collect all variables in a file you need to provide file root node"));

            }
        }
    }

    private fun filter(prop: KtProperty) = true

}

