package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.chapter3.spaces.asSequenceWithConcatenation
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.AbstractSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language

/**
 * Base class for testing rules without fixing code.
 * @property ruleSupplier mapping of list of [RulesConfig] into a [Rule]
 * @property rulesConfigList optional custom rules config
 */
open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
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
            expectedLintErrors.size == 1 && actualLintErrors.size == 1 -> assertThat(actualLintErrors[0])
                .describedAs(description)
                .isEqualTo(expectedLintErrors[0])

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

        KtLint.lint(
            KtLint.ExperimentalParams(
                fileName = actualFileName,
                script = actualFileName.endsWith("kts"),
                text = code,
                ruleSets = listOf(DiktatRuleSetProvider4Test(ruleSupplier,
                    rulesConfigList ?: this.rulesConfigList).get()),
                cb = { lintError, _ -> lintErrors += lintError },
            )
        )

        return lintErrors
    }

    /**
     * Tests multiple code [fragments] using the same
     * [rule configuration][rulesConfigList].
     *
     * All code fragments get concatenated together and the resulting, bigger
     * fragment gets tested, too.
     *
     * @param fragments the code fragments to check.
     * @param lintErrors the expected lint errors.
     * @param rulesConfigList the list of rules which can optionally override
     *   the [default value][LintTestBase.rulesConfigList].
     * @param fileName the optional override for a file name,
     * @see lintMethod
     */
    protected fun lintMultipleMethods(
        @Language("kotlin") fragments: Array<String>,
        vararg lintErrors: LintError,
        rulesConfigList: List<RulesConfig>? = null,
        fileName: String? = null
    ) {
        require(fragments.isNotEmpty()) {
            "code fragments is an empty array"
        }

        assertSoftly { softly ->
            fragments.asSequenceWithConcatenation().forEach { fragment ->
                softly.lintMethodSoftly(
                    fragment,
                    lintErrors = lintErrors,
                    rulesConfigList,
                    fileName
                )
            }
        }
    }

    /**
     * Similar to [lintMethod], but can be invoked from a scope of
     * `AbstractSoftAssertions` in order to accumulate test results from linting
     * _multiple_ code fragments.
     *
     * @param rulesConfigList the list of rules which can optionally override
     *   the [default value][LintTestBase.rulesConfigList].
     * @see lintMethod
     */
    private fun AbstractSoftAssertions.lintMethodSoftly(
        @Language("kotlin") code: String,
        vararg lintErrors: LintError,
        rulesConfigList: List<RulesConfig>? = null,
        fileName: String? = null
    ) {
        require(code.isNotBlank()) {
            "code is blank"
        }

        collectAssertionErrors {
            lintMethod(code, expectedLintErrors = lintErrors, rulesConfigList, fileName)
        }
    }
}
