package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression

class NoBracesLambdasWhenRule : Rule("no-braces-lambdas-when") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect


        //print(node.prettyPrint())

        if (node.elementType == ElementType.LAMBDA_EXPRESSION) {
            someFun(node.firstChildNode)
        }
    }


    private fun someFun(node: ASTNode) {
        if (node.hasChildOfType(ElementType.ARROW) && node.hasChildOfType(ElementType.BLOCK)) {
            val firstBlock = node.getFirstChildWithType(ElementType.BLOCK)!!
            if (firstBlock.hasChildOfType(ElementType.LAMBDA_EXPRESSION)) {
                checkBraces(firstBlock.firstChildNode.firstChildNode)
            }
        }
    }

    private fun checkBraces(node: ASTNode) {
        if (node.hasChildOfType(ElementType.LBRACE) && node.hasChildOfType(ElementType.RBRACE)) {
            Warnings.NO_BRACES_IN_LAMBDAS_AND_WHEN.warnAndFix(configRules, emitWarn, isFixMode, "text", node.startOffset) {

            }
        }
    }
}