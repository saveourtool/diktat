package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.unwrap
import org.cqfn.diktat.ktlint.lint
import org.cqfn.diktat.ruleset.rules.DiktatRule
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language

/**
 * Base class for testing rules without fixing code.
 * @property ruleSupplier mapping of list of [RulesConfig] into a [Rule]
 * @property rulesConfigList optional custom rules config
 */
open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> DiktatRule,
                        private val rulesConfigList: List<RulesConfig>? = null) {
    /**
     * Perform linting of [code], collect errors and compare with [expectedLintErrors]
     *
     * @param code code to check
     * @param expectedLintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @param fileName optional override for file name
     * @see lintResult
     */
    fun lintMethod(@Language("kotlin") code: String,
                   vararg expectedLintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = null,
                   fileName: String? = null
    ) {
        val actualLintErrors = lintResult(code, rulesConfigList, fileName)

        val description = "lint result for \"$code\""

        when {
            expectedLintErrors.size == 1 && actualLintErrors.size == 1 -> {
                val actual = actualLintErrors[0]
                val expected = expectedLintErrors[0]

                assertThat(actual)
                    .describedAs(description)
                    .isEqualTo(expected)
                assertThat(actual.canBeAutoCorrected)
                    .describedAs("canBeAutoCorrected")
                    .isEqualTo(expected.canBeAutoCorrected)
            }

            else -> assertThat(actualLintErrors)
                .describedAs(description)
                .apply {
                    when {
                        expectedLintErrors.isEmpty() -> isEmpty()
                        else -> containsExactly(*expectedLintErrors)
                    }
                }
        }
    }

    /**
     * Lints the [code] and returns the errors collected, but (unlike
     * [lintMethod]) doesn't make any assertions.
     *
     * @param code the code to check.
     * @param rulesConfigList an optional override for `this.rulesConfigList`.
     * @param fileName an optional override for the file name.
     * @return the list of lint errors.
     * @see lintMethod
     */
    protected fun lintResult(
        @Language("kotlin") code: String,
        rulesConfigList: List<RulesConfig>? = null,
        fileName: String? = null
    ): List<LintError> {
        val actualFileName = fileName ?: TEST_FILE_NAME
        val lintErrors: MutableList<LintError> = mutableListOf()

        lint(
            ruleSetSupplier = DiktatRuleSetProvider4Test(ruleSupplier, rulesConfigList ?: this.rulesConfigList),
            fileName = actualFileName,
            text = code,
            cb = { diktatError, _ -> lintErrors += diktatError.unwrap() }
        )

        return lintErrors
    }
}
