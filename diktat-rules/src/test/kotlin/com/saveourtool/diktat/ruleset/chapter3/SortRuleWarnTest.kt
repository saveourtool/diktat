package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_DECLARATIONS_ORDER
import com.saveourtool.diktat.ruleset.rules.chapter3.SortRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SortRuleWarnTest : LintTestBase(::SortRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${SortRule.NAME_ID}"
    private val rulesConfigNotSortEnum: List<RulesConfig> = listOf(
        RulesConfig(WRONG_DECLARATIONS_ORDER.name, true,
            mapOf("sortEnum" to "false"))
    )
    private val rulesConfigNotSortProperty: List<RulesConfig> = listOf(
        RulesConfig(WRONG_DECLARATIONS_ORDER.name, true,
            mapOf("sortProperty" to "false"))
    )
    private val rulesConfigNotSortBoth: List<RulesConfig> = listOf(
        RulesConfig(WRONG_DECLARATIONS_ORDER.name, true,
            mapOf("sortProperty" to "false", "sortEnum" to "false"))
    )

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check simple correct enum`() {
        lintMethod(
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
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check simple wrong enum`() {
        lintMethod(
            """
                    |enum class Alph {
                    |   D,
                    |   C,
                    |   A,
                    |   B,
                    |   ;
                    |}
            """.trimMargin(),
            DiktatError(1, 17, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} enum entries order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check correct enum`() {
        lintMethod(
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
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong enum without semicolon`() {
        lintMethod(
            """
                    |enum class ENUM {
                    |   GREEN(0x00FF00),
                    |   RED(0xFF0000),
                    |   BLUE(0x0000FF),
                    |}
            """.trimMargin(),
            DiktatError(1, 17, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} enum entries order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong enum without semicolon and last comma`() {
        lintMethod(
            """
                    |enum class ENUM {
                    |   GREEN(0x00FF00),
                    |   RED(0xFF0000),
                    |   BLUE(0x0000FF)
                    |}
            """.trimMargin(),
            DiktatError(1, 17, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} enum entries order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check correct enum without semicolon and last comma`() {
        lintMethod(
            """
                    |enum class ENUM {
                    |   BLUE(0x0000FF),
                    |   GREEN(0x00FF00),
                    |   RED(0xFF0000),
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong enum with fun`() {
        lintMethod(
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
            DiktatError(1, 21, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} enum entries order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong enum with fun but with config`() {
        lintMethod(
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
            """.trimMargin(), rulesConfigList = rulesConfigNotSortEnum
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong properties between non conts`() {
        lintMethod(
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
            DiktatError(4, 8, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} constant properties inside companion object order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong properties between non const more than one group`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       private val log = "Log"
                    |       private const val B = 4
                    |       private const val A = 5
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val Daa = 5
                    |       private const val Da = 5
                    |       private const val Db = 5
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(4, 8, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} constant properties inside companion object order is incorrect", true),
            DiktatError(7, 8, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} constant properties inside companion object order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong properties between non const more than one group only one`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       private val log = "Log"
                    |       private const val A = 4
                    |       private const val D = 5
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val Daa = 5
                    |       private const val Da = 5
                    |       private const val Db = 5
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(7, 8, ruleId, "${WRONG_DECLARATIONS_ORDER.warnText()} constant properties inside companion object order is incorrect", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong properties but with config`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       private val log = "Log"
                    |       private const val A = 4
                    |       private const val D = 5
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val Daa = 5
                    |       private const val Da = 5
                    |       private const val Db = 5
                    |   }
                    |}
            """.trimMargin(), rulesConfigList = rulesConfigNotSortProperty
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check wrong properties but with both config`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       private val log = "Log"
                    |       private const val A = 4
                    |       private const val D = 5
                    |       private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
                    |       private const val Daa = 5
                    |       private const val Da = 5
                    |       private const val Db = 5
                    |   }
                    |}
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
            """.trimMargin(), rulesConfigList = rulesConfigNotSortBoth
        )
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check correct simple properties`() {
        lintMethod(
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
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `check correct simple properties between non const`() {
        lintMethod(
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
