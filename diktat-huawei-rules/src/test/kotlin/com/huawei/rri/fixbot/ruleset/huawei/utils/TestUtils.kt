package com.huawei.rri.fixbot.ruleset.huawei.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.test.lint
import config.rules.RulesConfig
import org.assertj.core.api.Assertions

fun lintMethod(rule: Rule, code: String, vararg lintErrors: LintError) {
    Assertions.assertThat(
        rule.lint(code)
    ).containsExactly(
        *lintErrors
    )
}

internal fun Rule.format(text: String, fileName: String,
                         rulesConfigList: List<RulesConfig>? = emptyList()): String {
    return KtLint.format(
        KtLint.Params(
            text = text,
            ruleSets = listOf(RuleSet("huawei-codestyle", this@format)),
            fileName = fileName,
            rulesConfigList = rulesConfigList,
            cb = { _, _ -> }
        )
    )
}
