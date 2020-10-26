package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.classes.AbstractClassesRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AbstractClassesWarnTest : LintTestBase(::AbstractClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:abstract-classes"

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should not remove abstract`() {
        lintMethod(
                """
                    |abstract class Some(val a: Int = 5) {
                    |   abstract fun func() {}
                    |   
                    |   fun another() {}
                    |}
                """.trimMargin()
        )
    }


    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should remove abstract`() {
        lintMethod(
                """
                    |abstract class Some(val a: Int = 5) {
                    |    fun func() {}
                    |}
                """.trimMargin(),
                LintError(1, 37, ruleId, "${Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT.warnText()} Some", true)
        )
    }
}
