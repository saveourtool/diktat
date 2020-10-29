package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.ruleset.utils.search.getChildScopes
import org.cqfn.diktat.ruleset.utils.search.isShadowOf
import org.cqfn.diktat.util.applyToCode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("UnsafeCallOnNullableType")
class GetChildScopeTest {
    @Test
    fun `testing how getChildScopes work with functions`() {
        applyToCode("""
            fun foo(a: Int) { // parameters[0]
                fun foo1(a: Int) { // parameters[1]
                    println(a)
                    val a = 1 // properties[0]
                }
                
                fun foo2(a: Int) { // parameters[2]
                }
            }
            
            fun foo1(a: Int) { // parameters[3]
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == IDENTIFIER) {
                println(node.text)
                node.getChildScopes()
            }
        }
    }

    @Test
    fun `testing how getChildScopes work with classes`() {
        applyToCode("""
            val a = 5
            class A(val a: Int) { // parameters[0]
                class B(val a: Int) { // parameters[1]
                    fun foo(a: Int) { // parameters[2]
                        val a = 0 // properties[0]
                    }
                }
            }
            
            class B(val a: Int) { // parameters[3]
                fun foo(a: Int) { // parameters[4]
                    val a = 1 // properties[1]
                }
            }
        """.trimIndent(), 0) { node, _ ->
            if (node.elementType == IDENTIFIER) {
                println(node.text)
                node.getChildScopes()
            }
        }
    }
}

val a = 5
class A(val a: Int) { // parameters[0]
    class B(val a: Int) { // parameters[1]
        fun foo(a: Int) { // parameters[2]
            val a = 0 // properties[0]
        }
    }
}

class B(var a: Int) { // parameters[3]
    init {
        val a = 0 // properties[1]
    }

    companion object {
        val a = 0 // properties[2]
    }

    fun foo(a: Int) { // parameters[4]
        val a = 1 // properties[3]
    }
}