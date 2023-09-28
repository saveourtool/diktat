package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_LINES_IN_LAMBDA
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.2.5 check lambda length without parameters
 */
class LambdaLengthRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TOO_MANY_LINES_IN_LAMBDA)
) {
    private val configuration by lazy {
        LambdaLengthConfiguration(
            this.configRules.getRuleConfig(TOO_MANY_LINES_IN_LAMBDA)?.configuration ?: emptyMap()
        )
    }

    override fun logic(node: ASTNode) {
        if (node.elementType == KtNodeTypes.LAMBDA_EXPRESSION) {
            checkLambda(node, configuration)
        }
    }

    private fun checkLambda(node: ASTNode, configuration: LambdaLengthConfiguration) {
        val sizeLambda = countCodeLines(node)
        if (sizeLambda > configuration.maxLambdaLength && doesLambdaContainIt(node)) {
            TOO_MANY_LINES_IN_LAMBDA.warn(configRules, emitWarn,
                "max length lambda without arguments is ${configuration.maxLambdaLength}, but you have $sizeLambda",
                node.startOffset, node)
        }
    }

    /**
     * [RuleConfiguration] for lambda length
     */
    class LambdaLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed lambda length
         */
        val maxLambdaLength = config["maxLambdaLength"]?.toLong() ?: MAX_LINES_IN_LAMBDA
    }

    companion object {
        private const val MAX_LINES_IN_LAMBDA = 10L
        const val NAME_ID = "lambda-length"
    }
}
