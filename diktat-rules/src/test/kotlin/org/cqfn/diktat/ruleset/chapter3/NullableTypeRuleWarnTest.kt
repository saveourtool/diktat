package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import org.cqfn.diktat.ruleset.rules.NullableTypeRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class NullableTypeRuleWarnTest : LintTestBase(::NullableTypeRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:nullable-type"

    @Test
    fun `check simple property`() {
        lintMethod(
                """
                    |val a: List<Int>? = null
                    |val a: MutableList<Int>? = null
                """.trimMargin(),
                LintError(1,21, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
                LintError(2,28, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true)
        )
    }
}