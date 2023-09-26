package com.saveourtool.diktat.ruleset.rules.chapter4

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.SAY_NO_TO_VAR
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithAssignments
import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithUsages

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * Variables with `val` modifier - are immutable (read-only).
 * Usage of such variables instead of `var` variables increases robustness and readability of code,
 * because `var` variables can be reassigned several times in the business logic. Of course, in some scenarios with loops or accumulators only `var`s can be used and are allowed.
 * FixMe: here we should also raise warnings for a reassignment of a var (if var has no assignments except in declaration - it can be final)
 */
class ImmutableValNoVarRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(SAY_NO_TO_VAR)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            // we will raise warning for cases when var property has no assignments
            val varNoAssignments = node
                .findAllVariablesWithAssignments { it.name != null && it.isVar }
                .filter { it.value.isEmpty() }

            varNoAssignments.forEach { (_, _) ->
                // FixMe: raise another warning and fix the code (change to val) for variables without assignment
            }

            // we can force to be immutable only variables that are from local context (not from class and not from file-level)
            val usages = node
                .findAllVariablesWithUsages { it.isLocal && it.name != null && it.parent is KtBlockExpression && it.isVar }
                .filter { !varNoAssignments.containsKey(it.key) }

            usages.forEach { (property, usages) ->
                val usedInAccumulators = usages.any {
                    it.getParentOfType<KtLoopExpression>(true) != null ||
                            it.getParentOfType<KtLambdaExpression>(true) != null
                }

                if (!usedInAccumulators) {
                    SAY_NO_TO_VAR.warn(configRules, emitWarn, property.text, property.node.startOffset, property.node)
                }
            }

            return
        }
    }
    companion object {
        const val NAME_ID = "no-var-rule"
    }
}
