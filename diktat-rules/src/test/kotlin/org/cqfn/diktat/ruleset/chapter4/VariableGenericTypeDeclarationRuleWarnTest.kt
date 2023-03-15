package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter4.VariableGenericTypeDeclarationRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.WarningsNames.GENERIC_VARIABLE_WRONG_DECLARATION
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class VariableGenericTypeDeclarationRuleWarnTest : LintTestBase(::VariableGenericTypeDeclarationRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${VariableGenericTypeDeclarationRule.NAME_ID}"

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property with generic type good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   val myVariable: Map<Int, String> = emptyMap()
                    |   val lazyValue: Map<Int, String> by lazy {
                    |       println("computed!")
                    |       emptyMap<Int, String>()
                    |   }
                    |   val sideRegex = Regex("<([a-zA-Z, <>?]+)>")
                    |   val str = someMethod("mapOf<String>")
                    |   val x = foo.bar<Bar>().baz()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property with generic type bad`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   val myVariable: Map<Int, String> = emptyMap<Int, String>()
                    |   val any = Array<Any>(3) { "" }
                    |   val x = foo.bar<Bar>().baz<Some>()
                    |}
            """.trimMargin(),
            LintError(2, 4, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} type arguments are unnecessary in emptyMap<Int, String>()", true),
            LintError(3, 4, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} val any = Array<Any>(3) { \"\" }", false),
            LintError(4, 4, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} val x = foo.bar<Bar>().baz<Some>()", false)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function as parameter good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: Map<Int, String> = emptyMap()) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function as parameter with wildcard type good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: List<*> = emptyList<Int>()) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function as parameter with wildcard type good 2`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: Map<*, String> = emptyMap<Int, String>()) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function as parameter bad`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: Map<Int, String> = emptyMap<Int, String>()) {
                    |
                    |   }
                    |}
            """.trimMargin(),
            LintError(2, 25, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} type arguments are unnecessary in emptyMap<Int, String>()", true)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc() {
                    |       val myVariable: Map<Int, String> = emptyMap()
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function with wildcard type good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc() {
                    |       val myVariable: List<*> = emptyList<Int>()
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function bad`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc() {
                    |       val myVariable: Map<Int, String> = emptyMap<Int, String>()
                    |   }
                    |}
            """.trimMargin(),
            LintError(3, 8, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} type arguments are unnecessary in emptyMap<Int, String>()", true)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in class good`() {
        lintMethod(
            """
                    |class SomeClass(val myVariable: Map<Int, String> = emptyMap()) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in class with wildcard type good`() {
        lintMethod(
            """
                    |class SomeClass(val myVariable: List<*> = emptyList<Int>()) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in class bad`() {
        lintMethod(
            """
                    |class SomeClass(val myVariable: Map<Int, String> = emptyMap<Int, String>()) {
                    |
                    |}
            """.trimMargin(),
            LintError(1, 17, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} type arguments are unnecessary in emptyMap<Int, String>()", true)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property with multiple generics`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc() {
                    |       val myVariable: Map<Int, Map<String>> = emptyMap<Int, Map<String>>()
                    |   }
                    |}
            """.trimMargin(),
            LintError(3, 8, ruleId,
                "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} type arguments are unnecessary in emptyMap<Int, Map<String>>()", true)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `should not trigger`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   private fun someFunc() {
                    |       var myVariable: Map<Int, Any> = emptyMap<Int, String>()
                    |   }
                    |}
            """.trimMargin()
        )
    }
}
