package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

class RuleSetDiktat(val rulesConfig: List<RulesConfig>, vararg rules: Rule) : RuleSet(DIKTAT_RULE_SET_ID, *rules)

fun KtLint.Params.getDiktatConfigRules(): List<RulesConfig> {
    return (this.ruleSets.find { it.id == DIKTAT_RULE_SET_ID } as RuleSetDiktat).rulesConfig
}

class DiktatRuleSetProvider(private val jsonRulesConfig: String = "rules-config.json") : RuleSetProvider {
    override fun get(): RuleSet {
        return RuleSetDiktat(
            RulesConfigReader().readResource(jsonRulesConfig)?: listOf(),
            CommentsRule(),
            KdocComments(),
            KdocMethods(),
            KdocFormatting(),
            FileNaming(),
            PackageNaming(),
            IdentifierNaming(),
            FileStructureRule()  // this rule should be the last because it should operate on already valid code
        )
    }
}
