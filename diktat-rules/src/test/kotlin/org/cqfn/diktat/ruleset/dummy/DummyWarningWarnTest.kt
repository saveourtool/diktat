package org.cqfn.diktat.ruleset.dummy

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.DUMMY_TEST_WARNING
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * In this class you can test the logic of your rule, which should be implemented in DummyWarning
 */
class DummyWarningWarnTest : LintTestBase(::DummyWarning) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:dummy-rule"

    // Remove @Disabled annotation before usage
    @Disabled
    @Test
    @Tag(WarningNames.DUMMY_TEST_WARNING)
    fun `check dummy property`() {
        lintMethod(
            """
                |// provide your check here
            """.trimMargin(),
            LintError(1, 1, ruleId, "${DUMMY_TEST_WARNING.warnText()} some detailed explanation", true)
        )
    }
}
