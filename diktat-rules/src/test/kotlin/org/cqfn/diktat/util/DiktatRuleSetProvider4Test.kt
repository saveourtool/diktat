package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID

/**
 * simple class for emulating RuleSetProvider to inject .json rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                                 rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")

    override fun get(): RuleSet {
        return RuleSet(
                DIKTAT_RULE_SET_ID,
                ruleSupplier.invoke(rulesConfigList ?: emptyList())
        )
    }
}
