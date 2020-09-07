package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.slf4j.LoggerFactory

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

class DiktatRuleSetProvider(private val jsonRulesConfig: String = "rules-config.json") : RuleSetProvider {
    override fun get(): RuleSet {
        log.debug("Will run $DIKTAT_RULE_SET_ID with $jsonRulesConfig (it can be placed to the run directory or the default file from resources will be used)")
        val configRules = RulesConfigReader(javaClass.classLoader).readResource(jsonRulesConfig) ?: listOf()
        return RuleSet(
                DIKTAT_RULE_SET_ID,
                CommentsRule(configRules),
                KdocComments(configRules),
                KdocMethods(configRules),
                KdocFormatting(configRules),
                FileNaming(configRules),
                PackageNaming(configRules),
                FileSize(configRules),
                IdentifierNaming(configRules),
                ClassLikeStructuresOrderRule(configRules),
                BracesInConditionalsAndLoopsRule(configRules),
                BlockStructureBraces(configRules),
                EmptyBlock(configRules),
                EnumsSeparated(configRules),
                SingleLineStatementsRule(configRules),
                ConsecutiveSpacesRule(configRules),
                LongNumericalValuesSeparatedRule(configRules),
                AnnotationNewLineRule(configRules),
                HeaderCommentRule(configRules),
                SortRule(configRules),
                StringConcatenationRule(configRules),
                MultipleModifiersSequence(configRules),
                LineLength(configRules),
                BlankLinesRule(configRules),
                WhiteSpaceRule(configRules),
                WhenMustHaveElseRule(configRules),
                FileStructureRule(configRules),  // this rule should be right before indentation because it should operate on already valid code
                NewlinesRule(configRules),  // newlines need to be inserted right before fixing indentation
                IndentationRule(configRules)  // indentation rule should be the last because it fixes formatting after all the changes done by previous rules
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiktatRuleSetProvider::class.java)
    }
}
