package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.ENUMS_SEPARATED
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.EnumsSeparated
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EnumsSeparatedWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:enum-separated"

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check simple correct enum with new line`() {
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
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check simple correct enum with comments`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, // this is A
                    |   B,
                    |   C,
                    |   ;
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check simple enum but with fun` () {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, B, C;
                    |   fun foo() {}
                    |}
                """.trimMargin(),
                LintError(2,4,ruleId,"${ENUMS_SEPARATED.warnText()} enum entries must end with a line break", true),
                LintError(2,7,ruleId,"${ENUMS_SEPARATED.warnText()} enum entries must end with a line break", true),
                LintError(2,10,ruleId,"${ENUMS_SEPARATED.warnText()} semicolon must be on a new line", true),
                LintError(2,10,ruleId,"${ENUMS_SEPARATED.warnText()} last enum entry must end with a comma", true)
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check one line enum`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, B, C
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check wrong simple enum with new line last value`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, B, 
                    |   C
                    |}
                """.trimMargin(),
                LintError(2,4,ruleId,"${ENUMS_SEPARATED.warnText()} enum entries must end with a line break", true),
                LintError(3,4,ruleId,"${ENUMS_SEPARATED.warnText()} enums must end with semicolon", true),
                LintError(3,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum entry must end with a comma", true)
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check wrong simple enum with new line last value but with same line semicolon`() {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   A, B, 
                    |   C, ;
                    |}
                """.trimMargin(),
                LintError(2,4,ruleId,"${ENUMS_SEPARATED.warnText()} enum entries must end with a line break", true),
                LintError(3,4,ruleId,"${ENUMS_SEPARATED.warnText()} semicolon must be on a new line", true)
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check correct enum but with initialize entries` () {
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
    @Tag(WarningNames.ENUMS_SEPARATED)
    fun `check wrong enum with initialize entries and without last comma` () {
        lintMethod(EnumsSeparated(),
                """
                    |enum class ENUM {
                    |   RED(0xFF0000),
                    |   GREEN(0x00FF00),
                    |   BLUE(0x0000FF)
                    |   ;
                    |}
                """.trimMargin(),
                LintError(4,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum entry must end with a comma", true)
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
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
    @Tag(WarningNames.ENUMS_SEPARATED)
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
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum entry must end with a comma", true)
        )
    }

    @Test
    @Tag(WarningNames.ENUMS_SEPARATED)
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
                LintError(5,4,ruleId,"${ENUMS_SEPARATED.warnText()} last enum entry must end with a comma", true)

        )
    }
}
