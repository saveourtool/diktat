package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.SORT_RULE
import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.SortRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SortRuleWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:sort-rule"

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check simple correct enum`() {
        lintMethod(SortRule(),
                """
                    |enum class Alph {
                    |   A,
                    |   B,
                    |   C,
                    |   D,
                    |   ;
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check simple wrong enum`() {
        lintMethod(SortRule(),
                """
                    |enum class Alph {
                    |   D,
                    |   C,
                    |   A,
                    |   B,
                    |   ;
                    |}
                """.trimMargin(),
                LintError(1, 17, ruleId, "${SORT_RULE.warnText()} Enums order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check correct enum`() {
        lintMethod(SortRule(),
                """
                    |enum class ENUM {
                    |   BLUE(0x0000FF),
                    |   GREEN(0x00FF00),
                    |   RED(0xFF0000),
                    |   ;
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check wrong enum with fun`() {
        lintMethod(SortRule(),
                """
                    |enum class Warnings {
                    |   WAITING {
                    |      override fun signal() = TALKING
                    |   }, 
                    |   TALKING  {
                    |      override fun signal() = TALKING
                    |   },
                    |   ;
                    |   abstract fun signal(): ProtocolState
                    |}
                """.trimMargin(),
                LintError(1, 21, ruleId, "${SORT_RULE.warnText()} Enums order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check wrong properties between non conts`() {
        lintMethod(SortRule(),
                """
                    |class A {
                    |   companion object {
                    |       private val log = "Log"
                    |       private const val B = 4
                    |       private const val A = 5
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val A = 5
                    |   }
                    |}
                """.trimMargin(),
                LintError(2, 21, ruleId, "${SORT_RULE.warnText()} Constant properties inside companion object order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check correct simple properties`() {
        lintMethod(SortRule(),
                """
                    |class A {
                    |   companion object {
                    |       private const val A = 5
                    |       private const val B = 4
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `check correct simple properties between non const`() {
        lintMethod(SortRule(),
                """
                    |class A {
                    |   companion object {
                    |       private const val D = 4
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val B = 4
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val C = 4
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |   }
                    |}
                """.trimMargin()
        )
    }
}
