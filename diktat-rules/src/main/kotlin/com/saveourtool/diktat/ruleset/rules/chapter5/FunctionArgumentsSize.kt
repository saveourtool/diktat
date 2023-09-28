package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_PARAMETERS
import com.saveourtool.diktat.ruleset.rules.DiktatRule

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.KtFunction

/**
 * Rule that checks that function doesn't contains too many parameters
 */
class FunctionArgumentsSize(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TOO_MANY_PARAMETERS)
) {
    private val configuration: FunctionArgumentsSizeConfiguration by lazy {
        FunctionArgumentsSizeConfiguration(configRules.getRuleConfig(TOO_MANY_PARAMETERS)?.configuration ?: emptyMap())
    }

    override fun logic(node: ASTNode) {
        if (node.elementType == KtNodeTypes.FUN) {
            checkFun(node, configuration.maxParameterSize)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode, maxParameterSize: Long) {
        val parameterListSize = (node.psi as KtFunction).valueParameters.size
        if (parameterListSize > maxParameterSize) {
            TOO_MANY_PARAMETERS.warn(configRules, emitWarn,
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
        const val MAX_DEFAULT_PARAMETER_SIZE = 5L
        const val NAME_ID = "argument-size"
    }
}
