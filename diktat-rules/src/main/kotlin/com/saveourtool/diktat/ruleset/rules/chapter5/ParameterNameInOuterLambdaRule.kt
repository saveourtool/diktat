package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.doesLambdaContainIt
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.2.7 check parameter name in outer lambda
 */
class ParameterNameInOuterLambdaRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(PARAMETER_NAME_IN_OUTER_LAMBDA)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtNodeTypes.LAMBDA_EXPRESSION) {
            checkLambda(node)
        }
    }

    private fun checkLambda(node: ASTNode) {
        val hasInnerLambda = node
            .findAllDescendantsWithSpecificType(KtNodeTypes.LAMBDA_EXPRESSION, false)
            .isNotEmpty()
        if (hasInnerLambda && doesLambdaContainIt(node)) {
            PARAMETER_NAME_IN_OUTER_LAMBDA.warn(
                configRules, emitWarn, isFixMode,
                "lambda without arguments has inner lambda",
                node.startOffset, node,
            )
        }
    }
    companion object {
        const val NAME_ID = "parameter-name-in-outer-lambda"
    }
}
