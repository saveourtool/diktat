package org.cqfn.diktat.ruleset.rules.chapter5

import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.2.7 check parameter name in outer lambda
 */
class ParameterNameInOuterLambdaRule(configRules: List<RulesConfig>) : DiktatRule(
    "parameter-name-in-outer-lambda",
    configRules,
    listOf(PARAMETER_NAME_IN_OUTER_LAMBDA)
) {

    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.LAMBDA_EXPRESSION) {
            checkLambda(node)
        }
    }

    private fun checkLambda(node: ASTNode) {
        val copyNode = node.clone() as ASTNode

        val lambdaCount = copyNode.findAllNodesWithCondition { it.elementType == ElementType.LAMBDA_EXPRESSION }.size
        if (lambdaCount > 1) {
            copyNode.findAllNodesWithCondition { it.elementType == ElementType.LAMBDA_EXPRESSION }
                    .forEachIndexed { index, astNode ->
                        if (index > 0) {
                            astNode.treeParent.removeChild(astNode)
                        }
                    }
            val isIt = copyNode.findAllDescendantsWithSpecificType(ElementType.REFERENCE_EXPRESSION).map { re -> re.text }.contains("it")
            val parameters = node.findChildByType(ElementType.FUNCTION_LITERAL)?.findChildByType(ElementType.VALUE_PARAMETER_LIST)
            if (parameters == null && isIt) {
                PARAMETER_NAME_IN_OUTER_LAMBDA.warn(configRules, emitWarn, isFixMode,
                        "lambda without arguments has inner lambda",
                        node.startOffset, node)
            }
        }
    }
}
