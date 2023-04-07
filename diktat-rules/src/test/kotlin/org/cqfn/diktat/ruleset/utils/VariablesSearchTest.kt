package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.utils.search.VariablesSearch
import org.cqfn.diktat.ruleset.utils.search.default
import org.cqfn.diktat.util.applyToCode

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType != KtFileElementType.INSTANCE) {
                val thrown = Assertions.assertThrows(IllegalArgumentException::class.java) {
                    val variablesSearchAbstract: VariablesSearch = Mockito.mock(VariablesSearch::class.java, Mockito.CALLS_REAL_METHODS)
                    val nodeField = VariablesSearch::class.java.getDeclaredField("node")
                    val filter = VariablesSearch::class.java.getDeclaredField("filterForVariables")
                    nodeField.isAccessible = true
                    filter.isAccessible = true

                    nodeField.set(variablesSearchAbstract, node)
                    filter.set(variablesSearchAbstract, ::default)

                    variablesSearchAbstract.collectVariables()
                }
                assertTrue(thrown.message!!.contains("To collect all variables in a file you need to provide file root node"))
            }
        }
    }
}
