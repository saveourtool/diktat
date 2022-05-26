package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.FeatureInAlphaState

/**
 * Base class for testing rules without fixing code.
 * @property ruleSupplier mapping of list of [RulesConfig] into a [Rule]
 * @property rulesConfigList optional custom rules config
 */
open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>, String?) -> Rule,
                        private val rulesConfigList: List<RulesConfig>? = null) {
    /**
     * Perform linting of [code], collect errors and compare with [lintErrors]
     *
     * @param code code to check
     * @param lintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @param fileName optional override for file name
     */
    @OptIn(FeatureInAlphaState::class)
    fun lintMethod(code: String,
                   vararg lintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = null,
                   fileName: String? = null
    ) {
        val actualFileName = fileName ?: TEST_FILE_NAME
        val res: MutableList<LintError> = mutableListOf()
        KtLint.lint(
            KtLint.ExperimentalParams(
                fileName = actualFileName,
                script = actualFileName.endsWith("kts"),
                text = code,
                ruleSets = listOf(DiktatRuleSetProvider4Test(ruleSupplier,
                    rulesConfigList ?: this.rulesConfigList).get()),
                cb = { lintError, _ -> res.add(lintError) },
                userData = mapOf("file_path" to actualFileName)
            )
        )
        res.assertEquals(*lintErrors)
    }
}
