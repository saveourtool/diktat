package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_LAMBDAS
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NoBracesLambdasRule : Rule("no-braces-lambdas") {

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

        if (node.elementType == LAMBDA_EXPRESSION) {
            someFun(node.firstChildNode)
        }
    }


    private fun someFun(node: ASTNode) {
        if (node.hasChildOfType(ARROW) && node.hasChildOfType(BLOCK)) {
            val firstBlock = node.getFirstChildWithType(BLOCK)!!
            if (firstBlock.hasChildOfType(LAMBDA_EXPRESSION)) {
                checkBraces(firstBlock.firstChildNode.firstChildNode)
            }
        }
    }

    private fun checkBraces(node: ASTNode) {
        if (node.hasChildOfType(LBRACE) && node.hasChildOfType(RBRACE)) {
            NO_BRACES_IN_LAMBDAS.warnAndFix(configRules, emitWarn, isFixMode, "text", node.startOffset) {
                node.removeChild(node.getFirstChildWithType(LBRACE)!!)
                node.removeChild(node.getFirstChildWithType(RBRACE)!!)
            }
        }
    }
}