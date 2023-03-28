package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider


/**
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * @param diktatRuleSetFactory  the configuration file where all configurations for
 *   inspections and rules are stored.
 */
class DiktatRuleSetProvider(private val diktatRuleSetFactory: DiktatRuleSetFactory = DiktatRuleSetFactory()) : RuleSetProvider {
    override fun get(): RuleSet = KtLintRuleSetWrapper(diktatRuleSetFactory())
}
