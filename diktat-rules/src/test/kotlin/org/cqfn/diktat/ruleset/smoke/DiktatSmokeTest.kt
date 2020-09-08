package org.cqfn.diktat.ruleset.smoke

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.util.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

// fixme: run as a separate maven goal/module?
class DiktatSmokeTest : FixTestBase("test/smoke",
        { DiktatRuleSetProvider() },
        { lintError, _ -> unfixedLintErrors.add(lintError) },
        null
) {
    companion object {
        private val unfixedLintErrors: MutableList<LintError> = mutableListOf()
    }

    @BeforeEach
    fun `prepare accumulator for LintErrors`() {
        unfixedLintErrors.clear()
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #1`() {
        fixAndCompare("Example1Expected.kt", "Example1Test.kt")
        unfixedLintErrors.assertEquals(
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-naming", "${FILE_NAME_INCORRECT.warnText()} Example1Test.kt_copy", true), // todo this is a false one
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-naming", "${FILE_NAME_MATCH_CLASS.warnText()} Example1Test.kt_copy vs Example", true), // todo this is a false one
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false), // todo what's with offset?
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:kdoc-formatting", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(7, 14, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} Example", false),
                LintError(8, 5, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false)
        )
    }
}