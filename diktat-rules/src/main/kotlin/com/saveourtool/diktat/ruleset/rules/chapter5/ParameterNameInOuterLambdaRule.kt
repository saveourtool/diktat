package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.doesLambdaContainIt
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.hasExplicitIt
import com.saveourtool.diktat.ruleset.utils.hasItInLambda
import com.saveourtool.diktat.ruleset.utils.hasNoParameters

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
        val innerLambdaList = node.findAllDescendantsWithSpecificType(KtNodeTypes.LAMBDA_EXPRESSION, false)
        val hasInnerLambda = innerLambdaList.isNotEmpty()

        if ((hasNoParameters(node) || node.hasExplicitIt()) && (!hasInnerLambda ||
                innerLambdaList.all { innerLambda -> !hasItInLambda(innerLambda) })) {
            return
        }
        if (hasInnerLambda && doesLambdaContainIt(node)) {
            PARAMETER_NAME_IN_OUTER_LAMBDA.warn(
                configRules, emitWarn,
                "lambda without arguments has inner lambda",
                node.startOffset, node,
            )
        }
    }

    companion object {
        const val NAME_ID = "parameter-name-in-outer-lambda"
    }

    @Suppress("MISSING_KDOC_CLASS_ELEMENTS")
    class ParameterNameInOuterLambdaConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Flag (when false) which allows to use `it` in outer lambda, if in inner lambdas would be no `it`
         */
        val strictMode = (config["strictMode"] == "true")
    }
}
