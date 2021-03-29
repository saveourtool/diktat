package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.findChildrenMatching

import com.pinterest.ktlint.core.ast.ElementType.IF
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtIfExpression

class CollapseIfStatementsRule(configRules: List<RulesConfig>) : DiktatRule(
    "collapse-if",
    configRules,
    listOf(
        Warnings.COLLAPSE_IF_STATEMENTS
    )
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == IF) {
            val thenNode = (node.psi as KtIfExpression).then?.node
            val nestedIf = thenNode?.findChildrenMatching(IF) { true }?.firstOrNull()
            nestedIf?.let {
                Warnings.COLLAPSE_IF_STATEMENTS.warnAndFix(configRules, emitWarn, isFixMode,
                    "avoid using redundant nested if-statements", nestedIf.startOffset, nestedIf) {
                    // TODO: implement fix method
                }
            }
        }
    }
}
