package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.doesLambdaContainIt
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.2.7 check parameter name in outer lambda
 */
class ParameterNameInOuterLambdaRule(configRules: List<RulesConfig>, prevId: String?) : DiktatRule(
    "parameter-name-in-outer-lambda",
    configRules,
    listOf(PARAMETER_NAME_IN_OUTER_LAMBDA),
    prevId
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.LAMBDA_EXPRESSION) {
            checkLambda(node)
        }
    }

    private fun checkLambda(node: ASTNode) {
        val hasInnerLambda = node
            .findAllDescendantsWithSpecificType(ElementType.LAMBDA_EXPRESSION, false)
            .isNotEmpty()
        if (hasInnerLambda && doesLambdaContainIt(node)) {
            PARAMETER_NAME_IN_OUTER_LAMBDA.warn(
                configRules, emitWarn, isFixMode,
                "lambda without arguments has inner lambda",
                node.startOffset, node,
            )
        }
    }
}
