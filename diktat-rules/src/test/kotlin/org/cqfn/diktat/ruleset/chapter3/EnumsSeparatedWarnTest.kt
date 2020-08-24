package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.ENUMS_SEPARATED
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.EnumsSeparated
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class EnumsSeparatedWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:enum-separated"

    @Test
    fun `check simple correct enum`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A,
                    |   B,
                    |   C,
                    |   ;
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check one line enum`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, B, C
                    |}
                """.trimMargin(),
                LintError(2,4,ruleId,"${ENUMS_SEPARATED.warnText()} enum constance must end with a line break", true),
                LintError(2,7,ruleId,"${ENUMS_SEPARATED.warnText()} enum constance must end with a line break", true),
                LintError(2,10,ruleId,"${ENUMS_SEPARATED.warnText()} enums must end with semicolon", true),
                LintError(2,10,ruleId,"${ENUMS_SEPARATED.warnText()} last enum constance must end with a comma", true)
        )
    }

    @Test
    fun `check correct enum but with initialize constance` () {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   RED(0xFF0000),
                    |   GREEN(0x00FF00),
                    |   BLUE(0x0000FF),
                    |   ;
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check wrong enum with initialize constance and without last comma` () {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   RED(0xFF0000),
                    |   GREEN(0x00FF00),
                    |   BLUE(0x0000FF)
                    |   ;
                    |}
                """.trimMargin(),
                LintError(4,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum constance must end with a comma", true)
        )
    }

    @Test
    fun `check correct enum with method`() {
        lintMethod(EnumsSeparated(),
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
                """.trimMargin()
        )
    }


    @Test
    fun `check wrong enum without last comma and line break`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class Warnings {
                    |   WAITING {
                    |      override fun signal() = TALKING
                    |   }, 
                    |   TALKING  {
                    |      override fun signal() = TALKING
                    |   };
                    |   abstract fun signal(): ProtocolState
                    |}
                """.trimMargin(),
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} semicolon must be on a new line", true),
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum constance must end with a comma", true)
        )
    }

    @Test
    fun `check wrong enum without last comma, line break and semicolon`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class Warnings {
                    |   WAITING {
                    |      override fun signal() = TALKING
                    |   },
                    |   TALKING  {
                    |      override fun signal() = TALKING
                    |   }
                    |}
                """.trimMargin(),
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} enums must end with semicolon", true),
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum constance must end with a comma", true)

        )
    }
}
