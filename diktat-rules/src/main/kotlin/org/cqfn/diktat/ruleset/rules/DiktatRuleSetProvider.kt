package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.slf4j.LoggerFactory
import java.nio.file.Paths

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

class RuleSetDiktat(val rulesConfig: List<RulesConfig>, vararg rules: Rule) : RuleSet(DIKTAT_RULE_SET_ID, *rules)

fun KtLint.Params.getDiktatConfigRules(): List<RulesConfig> = (this.ruleSets.find { it.id == DIKTAT_RULE_SET_ID } as RuleSetDiktat).rulesConfig

class DiktatRuleSetProvider(private val jsonRulesConfig: String = "rules-config.json") : RuleSetProvider {
    override fun get(): RuleSet {
        log.debug("Will run $DIKTAT_RULE_SET_ID with $jsonRulesConfig (it can be placed to the run directory or the default file from resources will be used)")
        return RuleSetDiktat(
            RulesConfigReader(javaClass.classLoader).readResource(jsonRulesConfig) ?: listOf(),
            CommentsRule(),
            KdocComments(),
            KdocMethods(),
            KdocFormatting(),
            FileNaming(),
            PackageNaming(),
            FileSize(),
            IdentifierNaming(),
            BracesInConditionalsAndLoopsRule(),
            BlockStructureBraces(),
            EmptyBlock(),
            FileStructureRule()  // this rule should be the last because it should operate on already valid code
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiktatRuleSetProvider::class.java)
    }
}
