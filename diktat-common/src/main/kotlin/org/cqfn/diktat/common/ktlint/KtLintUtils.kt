@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package org.cqfn.diktat.common.ktlint

private val ktlintRuleSetIds: List<String> by lazy {
    listOf(
        "standard",
        "experimental",
        "test",
        "custom"
    )
}

/**
 * Contains the necessary value of the `--disabled_rules` _KtLint_ argument.
 */
val ktlintDisabledRulesArgument: String by lazy {
    ktlintRuleSetIds.joinToString(
        prefix = "--disabled_rules=",
        separator = ","
    )
}
