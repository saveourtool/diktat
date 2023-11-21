package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.ruleset.utils.search.VariablesSearch
import com.saveourtool.diktat.ruleset.utils.search.default
import com.saveourtool.diktat.util.applyToCode
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
                val variablesSearchAbstract: VariablesSearch = object : VariablesSearch(node, ::default) {
                    override fun KtElement.getAllSearchResults(property: KtProperty): List<KtNameReferenceExpression> = TODO("Not required for test")
                }

                val thrown = Assertions.assertThrows(IllegalArgumentException::class.java) {
                    variablesSearchAbstract.collectVariables()
                }
                assertTrue(thrown.message!!.contains("To collect all variables in a file you need to provide file root node"))
            }
        }
    }
}
