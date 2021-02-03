package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.utils.log

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private typealias DiktatConfigRule = org.cqfn.diktat.common.config.rules.Rule

/**
 * This is a wrapper around Ktlint Rule
 *
 * @param id id of the rule
 * @property configRules all rules from configuration
 * @property rules warnings that are used in the rule's code
 */
abstract class DiktatRule(id: String,
                          val configRules: List<RulesConfig>,
                          val rules: List<DiktatConfigRule>) : Rule(id) {
    /**
     * Default value is false
     */
    var isFixMode: Boolean = false

    /**
     * Will be initialized in visit
     */
    lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (areRulesEnabled()) {
            return
        } else {
            try {
                logic(node)
            } catch (internalError: Throwable) {
                log.error("Internal error has occurred in $id. Please make an issue on this bug at https://github.com/cqfn/diKTat/.\n Error: ${internalError.message}")
            }
        }
    }

    private fun areRulesEnabled(): Boolean =
            rules.none { configRules.isRuleEnabled(it) }

    /**
     * Logic of the rule
     *
     * @param node node that are coming from visit
     */
    abstract fun logic(node: ASTNode)
}
