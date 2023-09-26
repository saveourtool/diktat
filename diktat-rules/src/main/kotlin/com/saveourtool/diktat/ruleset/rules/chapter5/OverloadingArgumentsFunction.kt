package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_OVERLOADING_FUNCTION_ARGUMENTS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.allSiblings
import com.saveourtool.diktat.ruleset.utils.findChildAfter
import com.saveourtool.diktat.ruleset.utils.findChildBefore

import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Rule that suggests to use functions with default parameters instead of multiple overloads
 */
class OverloadingArgumentsFunction(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
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
            .filter { it.isOverloadedBy(funPsi) }
            .filter { it.hasSameModifiers(funPsi) }
            .filter { funPsi.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text }
            .filter { funPsi.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text }
            .toList()

        if (allOverloadFunction.isNotEmpty()) {
            WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warn(configRules, emitWarn, funPsi.node.findChildByType(IDENTIFIER)!!.text, funPsi.startOffset, funPsi.node)
        }
    }

    /**
     * We can raise errors only on those methods that have same modifiers (inline/public/etc.)
     */
    private fun KtFunction.hasSameModifiers(other: KtFunction) =
        this.getSortedModifiers() ==
                other.getSortedModifiers()

    private fun KtFunction.getSortedModifiers() = this.modifierList
        ?.node
        ?.getChildren(KtTokens.MODIFIER_KEYWORDS)
        ?.map { it.text }
        ?.sortedBy { it }

    /**
     * we need to compare following things for two functions:
     * 1) that function arguments go in the same order in both method
     * 2) that arguments have SAME names (you can think that it is not necessary,
     * but usually if developer really wants to overload method - he will have same names of arguments)
     * 3) arguments have same types (obviously)
     *
     * So we need to find methods with following arguments: foo(a: Int, b: Int) and foo(a: Int). foo(b: Int) is NOT suitable
     */
    private fun KtFunction.isOverloadedBy(other: KtFunction): Boolean {
        // no need to process methods with different names
        if (this.nameIdentifier?.text != other.nameIdentifier?.text) {
            return false
        }
        // if this function has more arguments, than other, then we will compare it on the next iteration cycle (at logic() level)
        // this hack will help us to point only to one function with smaller number of arguments
        if (this.valueParameters.size < other.valueParameters.size) {
            return false
        }

        for (i in 0 until other.valueParameters.size) {
            // all arguments on the same position should match by name and type
            if (this.valueParameters[i].getFunctionName() != other.valueParameters[i].getFunctionName() ||
                    this.valueParameters[i].getFunctionType() != other.valueParameters[i].getFunctionType()
            ) {
                return false
            }
        }
        return true
    }

    private fun KtParameter.getFunctionName() = this.nameIdentifier?.text
    private fun KtParameter.getFunctionType() = this.typeReference?.text

    companion object {
        const val NAME_ID = "overloading-default-values"
    }
}
