package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

/**
 * A group of [RuleProvider]'s discoverable through [RuleSetProviderV2].
 */
interface DiktatRuleSet {
    /**
     * The rule providers.
     */
    val ruleProviders: Set<RuleProvider>
}
