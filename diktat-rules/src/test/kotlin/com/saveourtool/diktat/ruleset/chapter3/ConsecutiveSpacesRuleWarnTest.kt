package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_CONSECUTIVE_SPACES
import com.saveourtool.diktat.ruleset.rules.chapter3.ConsecutiveSpacesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ConsecutiveSpacesRuleWarnTest : LintTestBase(::ConsecutiveSpacesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ConsecutiveSpacesRule.NAME_ID}"
    private val rulesConfigListNoSpaces: List<RulesConfig> = listOf(
        RulesConfig(TOO_MANY_CONSECUTIVE_SPACES.name, true,
            mapOf("maxSpaces" to "2"))
    )

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `enum spaces check bad`() {
        lintMethod(
            """
                    |enum       class IntArithmetics : BinaryOperator<Int> {
                    |    PLUS, ASD
                    |}
            """.trimMargin(),
            DiktatError(1, 5, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 7. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `enum spaces check good`() {
        lintMethod(
            """
                    |enum class SomeEnum {
                    |    PLUS
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `fun space check good`() {
        lintMethod(
            """
                    |class A {
                    |   fun testFunction(val a = 5) {
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `fun space check bad`() {
        lintMethod(
            """
                    |class A {
                    |   fun      testFunction(val a =     6) {
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 7, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 6. need to be: 1", true),
            DiktatError(2, 33, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `class space check bad`() {
        lintMethod(
            """
                    |class     SomeClass {
                    |   inner     class InnerClass{
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(1, 6, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true),
            DiktatError(2, 9, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `class space check good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   inner class InnerClass{
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `property space check good`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   fun someFunc() {
                    |       val a = 5
                    |       val b: Int = 3
                    |       val c: Int
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `property space check bad`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   fun someFunc() {
                    |       val a =    5
                    |       val b: Int     = 3
                    |       val c:     Int
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(3, 15, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 4. need to be: 1", true),
            DiktatError(4, 18, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true),
            DiktatError(5, 14, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)

        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `generic space check good`() {
        lintMethod(
            """
                    |class Box<T>(t: T){
                    |   var value = t
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `generic space check bad`() {
        lintMethod(
            """
                    |class Box<   T>(t:    T){
                    |   var value = t
                    |}
            """.trimMargin(),
            DiktatError(1, 11, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 3. need to be: 1", true),
            DiktatError(1, 19, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 4. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `interface space check good`() {
        lintMethod(
            """
                    |interface TestInterface{
                    |   fun foo()
                    |   fun bar()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `interface space check bad`() {
        lintMethod(
            """
                    |interface       TestInterface{
                    |   fun foo()
                    |   fun bar()
                    |}
            """.trimMargin(),
            DiktatError(1, 10, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 7. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `init space check bad`() {
        lintMethod(
            """
                    |class SomeClass{
                    |   init     {
                    |       print("SomeThing")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 8, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `init space check good`() {
        lintMethod(
            """
                    |class SomeClass{
                    |   init {
                    |       print("SomeThing")
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `config check`() {
        lintMethod(
            """
                    |class  SomeClass  {
                    |   init  {
                    |       print("SomeThing")
                    |   }
                    |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListNoSpaces
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `eol comment check`() {
        lintMethod(
            """
                    |class SomeClass{              // this is a comment
                    |   val a = 5 // this is another comment
                    |}
            """.trimMargin()
        )
    }
}
