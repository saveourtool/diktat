package org.cqfn.diktat.util

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig

open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                        private val rulesConfigList: List<RulesConfig>? = null) {
    fun lintMethod(code: String,
                   vararg lintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = null,
                   fileName: String? = null) {
        val res = mutableListOf<LintError>()
        val actualFileName = fileName ?: testFileName
        KtLint.lint(
                KtLint.Params(
                        fileName = actualFileName,
                        text = code,
                        ruleSets = listOf(DiktatRuleSetProvider4Test(ruleSupplier,
                                rulesConfigList ?: this.rulesConfigList).get()),
                        cb = { e, _ -> res.add(e) },
                        userData = mapOf("file_path" to actualFileName)
                )
        )
        res.assertEquals(*lintErrors)
    }
}
