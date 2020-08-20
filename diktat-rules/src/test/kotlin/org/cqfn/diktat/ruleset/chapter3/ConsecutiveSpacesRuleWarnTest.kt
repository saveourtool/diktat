package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.ConsecutiveSpacesRule
import org.cqfn.diktat.util.lintMethod
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_CONSECUTIVE_SPACES
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test


class ConsecutiveSpacesRuleWarnTest {


    private val ruleId = "$DIKTAT_RULE_SET_ID:too-many-spaces"


    private val rulesConfigListNoSpaces: List<RulesConfig> = listOf(
            RulesConfig(TOO_MANY_CONSECUTIVE_SPACES.name, true,
                    mapOf("max_spaces" to "2"))
    )

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `enum spaces check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |enum       class IntArithmetics : BinaryOperator<Int> {
                    |    PLUS, ASD
                    |}
                """.trimMargin(),
                LintError(1, 5, ruleId, "${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 7. need to be: 1", true)
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `enum spaces check good`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |enum class SomeEnum {
                    |    PLUS
                    |}
                """.trimMargin()
        )
    }


    @Test
    @Tag("TOO_MANY_SPACES")
    fun `fun space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class A {
                    |   fun testFunction(val a = 5) {
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `fun space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class A {
                    |   fun      testFunction(val a =     6) {
                    |   }
                    |}
                """.trimMargin(),
                LintError(2, 7 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 6. need to be: 1", true),
                LintError(2, 33 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `class space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class     SomeClass {
                    |   inner     class InnerClass{
                    |   }
                    |}
                """.trimMargin(),
                LintError(1, 6 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true),
                LintError(2, 9 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `class space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class SomeClass {
                    |   inner class InnerClass{
                    |   }
                    |}
                """.trimMargin()
        )
    }


    @Test
    @Tag("TOO_MANY_SPACES")
    fun `property space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
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
    @Tag("TOO_MANY_SPACES")
    fun `property space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class SomeClass {
                    |   fun someFunc() {
                    |       val a =    5
                    |       val b: Int     = 3
                    |       val c:     Int
                    |   }
                    |}
                """.trimMargin(),
                LintError(3, 15 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 4. need to be: 1", true),
                LintError(4, 18 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true),
                LintError(5, 14 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)

        )
    }


    @Test
    @Tag("TOO_MANY_SPACES")
    fun `generic space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class Box<T>(t: T){
                    |   var value = t
                    |}
                """.trimMargin()
                )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `generic space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class Box<   T>(t:    T){
                    |   var value = t
                    |}
                """.trimMargin(),
                LintError(1, 11 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 3. need to be: 1", true),
                LintError(1, 19 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 4. need to be: 1", true)
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `interface space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |interface TestInterface{
                    |   fun foo()
                    |   fun bar()
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `interface space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |interface       TestInterface{
                    |   fun foo()
                    |   fun bar()
                    |}
                """.trimMargin(),
                LintError(1, 10 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 7. need to be: 1", true)
        )
    }


    @Test
    @Tag("TOO_MANY_SPACES")
    fun `init space check bad`() {
        lintMethod(ConsecutiveSpacesRule(),
                """
                    |class SomeClass{
                    |   init     {
                    |       print("SomeThing")
                    |   }
                    |}
                """.trimMargin(),
                LintError(2, 8 , ruleId,"${TOO_MANY_CONSECUTIVE_SPACES.warnText()} found: 5. need to be: 1", true)
        )
    }


    @Test
    @Tag("TOO_MANY_SPACES")
    fun `init space check good`() {
        lintMethod(ConsecutiveSpacesRule(),
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
    @Tag("TOO_MANY_SPACES")
    fun `config check`() {
        lintMethod(ConsecutiveSpacesRule(),
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

}
