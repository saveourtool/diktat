package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.TYPE_ALIAS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.TypeAliasRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class TypeAliasRuleWarnTest : LintTestBase(::TypeAliasRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:type-alias"

    @Test
    fun `string concatenation - only strings`() {
        lintMethod(
                """
                    | val b: MutableMap<String, MutableList<String>>
                    | val b = listof<Int>()
                """.trimMargin(),
                LintError(1,9, ruleId, "${TYPE_ALIAS.warnText()} too long type reference, can be replace by type alias", false)
        )
    }
}