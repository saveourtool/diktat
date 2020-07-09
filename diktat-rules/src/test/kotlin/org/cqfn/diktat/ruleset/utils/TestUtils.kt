package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.Assertions
import org.cqfn.diktat.common.config.rules.RulesConfig

const val TEST_FILE_NAME = "/TestFileName.kt"

fun lintMethod(rule: Rule,
               code: String,
               vararg lintErrors: LintError,
               rulesConfigList: List<RulesConfig>? = null) {
    val res = mutableListOf<LintError>()
    KtLint.lint(
        KtLint.Params(
            fileName = TEST_FILE_NAME,
            text = code,
            ruleSets = listOf(DiktatRuleSetProviderTest(rule, rulesConfigList).get()),
            cb = { e, _ -> res.add(e) }
        )
    )
    Assertions.assertThat(
        res
    ).containsExactly(
        *lintErrors
    )
}

internal fun Rule.format(text: String, fileName: String,
                         rulesConfigList: List<RulesConfig>? = emptyList()): String {
    return KtLint.format(
        KtLint.Params(
            text = text,
            ruleSets = listOf(DiktatRuleSetProviderTest(this, rulesConfigList).get()),
            fileName = fileName,
            cb = { lintError, _ ->
                log.warn("Received linting error: $lintError")
            }
        )
    )
}
