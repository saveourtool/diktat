package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_OVERLOADING_FUNCTION_ARGUMENTS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.allSiblings
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore

import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Rule that suggests to use functions with default parameters instead of multiple overloads
 */
class OverloadingArgumentsFunction(configRules: List<RulesConfig>) : DiktatRule(
    "aby-overloading-default-values",
    configRules,
    listOf(WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == FUN) {
            checkFun(node.psi as KtFunction)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(funPsi: KtFunction) {
        val allOverloadFunction = funPsi
            .node
            .allSiblings(withSelf = false)
            .asSequence()
            .filter { it.elementType == FUN }
            .map { it.psi as KtFunction }
            .filter { it.nameIdentifier!!.text == funPsi.nameIdentifier!!.text && it.valueParameters.containsAll(funPsi.valueParameters) }
            .filter { funPsi.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text }
            .filter { funPsi.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text }
            .toList()
        if (allOverloadFunction.isNotEmpty()) {
            WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warn(configRules, emitWarn, isFixMode, funPsi.node.findChildByType(IDENTIFIER)!!.text, funPsi.startOffset, funPsi.node)
        }
    }
}
