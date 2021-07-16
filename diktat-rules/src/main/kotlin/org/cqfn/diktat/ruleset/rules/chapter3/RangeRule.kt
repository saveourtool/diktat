package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.RANGE_TO_UNTIL
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.takeByChainOfTypes

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.LOOP_RANGE
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.RANGE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This rule warn and fix cases when it possible to replace range with until
 */
class RangeRule(configRules: List<RulesConfig>) : DiktatRule(
    "until",
    configRules,
    listOf(RANGE_TO_UNTIL)) {
    override fun logic(node: ASTNode) {
        (node.takeByChainOfTypes(LOOP_RANGE, BINARY_EXPRESSION, OPERATION_REFERENCE, RANGE)
            ?: node.takeByChainOfTypes(LOOP_RANGE, BINARY_EXPRESSION, BINARY_EXPRESSION, OPERATION_REFERENCE, RANGE))
            ?.let {
                RANGE_TO_UNTIL.warnAndFix(configRules, emitWarn, isFixMode, node.text, it.startOffset, it) {
                    val untilNode = LeafPsiElement(IDENTIFIER, "until")
                    it.treeParent.treeParent.addChild(PsiWhiteSpaceImpl(" "), it.treeParent)
                    it.treeParent.addChild(untilNode, it)
                    it.treeParent.treeParent.addChild(PsiWhiteSpaceImpl(" "), it.treeParent.treeNext)
                    it.treeParent.removeChild(it)
                }
            }
    }
}
