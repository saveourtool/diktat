package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

/**
 * Test for [DiktatRuleSetProvider] in autocorrect mode as a whole. All rules are applied to a file.
 * Note: ktlint uses initial text from a file to calculate line and column from offset. Because of that line/col of unfixed errors
 * may change after some changes to text or other rules.
 */
class DiktatSmokeTest : DiktatSmokeTestBase() {
    override val isLintErrors = true
    override fun fixAndCompare(
        config: String,
        expected: String,
        test: String,
    ) {
        fixAndCompare(expected, test, emptyList(), true)
    }
}
