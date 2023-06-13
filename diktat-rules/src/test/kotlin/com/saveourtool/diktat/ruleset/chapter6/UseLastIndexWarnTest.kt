package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.UseLastIndex
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UseLastIndexWarnTest : LintTestBase(::UseLastIndex) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${UseLastIndex.NAME_ID}"

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 with many dot expressions`() {
        lintMethod(
            """
                    |val A = "AAAAAAAA"
                    |val D = A.B.C.length - 1
                    |
            """.trimMargin(),
            DiktatError(2, 9, ruleId, "${Warnings.USE_LAST_INDEX.warnText()} A.B.C.length - 1", true)
        )
    }

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 for mane line`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val A : String = "AAAA"
                    |   var B = A.length
                    |   -
                    |   1
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 with many spaces and tabulation`() {
        lintMethod(
            """
                    |val A : String = "AAAA"
                    |var B =    A.length   -       1  + 214
                    |var C = A.length - 19
                    |
            """.trimMargin(),
            DiktatError(2, 12, ruleId, "${Warnings.USE_LAST_INDEX.warnText() } A.length   -       1", true)
        )
    }

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 without spaces`() {
        lintMethod(
            """
                    |val A : String = "AAAA"
                    |var B = A.length-1
                    |
            """.trimMargin(),
            DiktatError(2, 9, ruleId, "${Warnings.USE_LAST_INDEX.warnText()} A.length-1", true)
        )
    }

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 without length`() {
        lintMethod(
            """
                    |val A = "AAAA"
                    |val B = -1
                    |val C = 6 + 121
                    |var D = B + C
                    |var E = A.length + 1
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.USE_LAST_INDEX)
    fun `find method Length - 1 without -1`() {
        lintMethod(
            """
                    |val A = "AAAA"
                    |val B = -1
                    |val C = 6 + 4
                    |val D = "AAAA".length - 1
                    |
                    |val M = "ASDFG".length
                    |
            """.trimMargin(),
            DiktatError(4, 9, ruleId, "${Warnings.USE_LAST_INDEX.warnText()} \"AAAA\".length - 1", true)
        )
    }
}
