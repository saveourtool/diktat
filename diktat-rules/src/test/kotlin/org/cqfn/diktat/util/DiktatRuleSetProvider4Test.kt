package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.RuleSetDiktat

/**
 * simple class for emulating RuleSetProvider to inject .json rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(val rule: Rule, rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("rules-config.json")

    override fun get(): RuleSet {
        return RuleSetDiktat(
                rulesConfigList?: listOf(),
                rule
        )
    }
}
