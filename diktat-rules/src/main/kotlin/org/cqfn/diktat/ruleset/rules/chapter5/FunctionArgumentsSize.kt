package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_PARAMETERS
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

/**
 * Rule that checks that function doesn't contains too many parameters
 */
class FunctionArgumentsSize(configRules: List<RulesConfig>) : DiktatRule(
    nameId,
    configRules,
    listOf(TOO_MANY_PARAMETERS)
) {
    private val configuration: FunctionArgumentsSizeConfiguration by lazy {
        FunctionArgumentsSizeConfiguration(configRules.getRuleConfig(TOO_MANY_PARAMETERS)?.configuration ?: emptyMap())
    }

    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.FUN) {
            checkFun(node, configuration.maxParameterSize)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode, maxParameterSize: Long) {
        val parameterListSize = (node.psi as KtFunction).valueParameters.size
        if (parameterListSize > maxParameterSize) {
            TOO_MANY_PARAMETERS.warn(configRules, emitWarn, isFixMode,
                "${node.findChildByType(IDENTIFIER)!!.text} has $parameterListSize, but allowed $maxParameterSize", node.startOffset, node)
        }
    }

    /**
     * [RuleConfiguration] for maximum number of parameters
     */
    class FunctionArgumentsSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed number of function parameters
         */
        val maxParameterSize = config["maxParameterListSize"]?.toLongOrNull() ?: MAX_DEFAULT_PARAMETER_SIZE
    }

    companion object {
        val nameId = "acc-argument-size"
        const val MAX_DEFAULT_PARAMETER_SIZE = 5L
    }
}
