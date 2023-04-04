package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LAMBDA_IS_NOT_LAST_PARAMETER
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_TYPE
import org.jetbrains.kotlin.KtNodeTypes.IDENTIFIER
import org.jetbrains.kotlin.KtNodeTypes.NULLABLE_TYPE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

/**
 * Rule that checks if parameter with function type is the last in parameter list
 */
class LambdaParameterOrder(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(LAMBDA_IS_NOT_LAST_PARAMETER)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.FUN) {
            checkArguments(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkArguments(node: ASTNode) {
        val funArguments = (node.psi as KtFunction).valueParameters
        val sortArguments = funArguments
            .sortedBy { valueParam ->
                valueParam
                    .typeReference
                    ?.node
                    ?.let { it.findChildByType(NULLABLE_TYPE) ?: it }
                    ?.hasChildOfType(FUNCTION_TYPE)
            }
        funArguments.filterIndexed { index, ktParameter -> ktParameter != sortArguments[index] }.ifNotEmpty {
            LAMBDA_IS_NOT_LAST_PARAMETER.warn(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text,
                first().node.startOffset, node)
        }
    }

    companion object {
        const val NAME_ID = "lambda-parameter-order"
    }
}
