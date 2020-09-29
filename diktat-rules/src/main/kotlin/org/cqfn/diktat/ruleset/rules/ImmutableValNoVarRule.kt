package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SAY_NO_TO_VAR
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllUsages
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * Variables with `val` modifier - are immutable (read-only).
 * Usage of such variables instead of `var` variables increases robustness and readability of code,
 * because `var` variables can be reassigned several times in the business logic. Of course, in some scenarios with loops or accumulators only `var`s can be used and are allowed.
 * FixMe: here we should also raise warnings for a reassignment of a var (if var has no assignments except in declaration - it can be final)
 */
class ImmutableValNoVarRule(private val configRules: List<RulesConfig>) : Rule("no-var-rule") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.FUN) {
            node.findAllNodesWithSpecificType(ElementType.PROPERTY)
                    .map { it.psi as KtProperty }
                    // we can force to be immutable only variables that are from local context (not from class and not from file-level)
                    .filter { it.isLocal && it.name != null && it.parent is KtBlockExpression }
                    .filter { it.isVar }
                    .forEach { property ->
                        val usedInAccumulators = property.getAllUsages().any {
                            it.getParentOfType<KtLoopExpression>(true) != null ||
                                    it.getParentOfType<KtLambdaExpression>(true) != null
                        }

                        if (!usedInAccumulators) {
                            SAY_NO_TO_VAR.warn(configRules, emitWarn, isFixMode, property.text, property.node.startOffset, property.node)
                        }
                    }
        }
    }
}
