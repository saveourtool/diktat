package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.FeatureInAlphaState
import org.intellij.lang.annotations.Language

/**
 * Base class for testing rules without fixing code.
 * @property ruleSupplier mapping of list of [RulesConfig] into a [Rule]
 * @property rulesConfigList optional custom rules config
 */
open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                        private val rulesConfigList: List<RulesConfig>? = null) {
    /**
     * Perform linting of [code], collect errors and compare with [lintErrors]
     *
     * @param code code to check
     * @param lintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @param fileName optional override for file name
     * @see lintResult
     */
    fun lintMethod(@Language("kotlin") code: String,
                   vararg lintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = null,
                   fileName: String? = null
    ) {
        lintResult(code, rulesConfigList, fileName)
            .assertEquals(*lintErrors)
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
    @OptIn(FeatureInAlphaState::class)
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
}
