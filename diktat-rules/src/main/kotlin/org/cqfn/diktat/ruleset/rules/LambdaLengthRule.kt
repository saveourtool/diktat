package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.2.5 check lambda length without parameters
 */
class LambdaLengthRule(private val configRules: List<RulesConfig>) : Rule("lambda-length") {
    private val configuration by lazy {
        LambdaLengthConfiguration(
            this.configRules.getRuleConfig(Warnings.TOO_MANY_LINES_IN_LAMBDA)?.configuration ?: emptyMap()
        )
    }
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.LAMBDA_EXPRESSION) {
            checkLambda(node, configuration)
        }
    }

    private fun checkLambda(node: ASTNode, configuration: LambdaLengthConfiguration) {
        val copyNode = node.clone() as ASTNode
        val sizeLambda = countCodeLines(copyNode)
        if (sizeLambda > configuration.maxLambdaLength) {
            copyNode.findAllNodesWithCondition({ it.elementType == ElementType.LAMBDA_EXPRESSION }).forEachIndexed { index, node ->
                if (index > 0) {
                    node.treeParent.removeChild(node)
                }
            }
            val isIt = copyNode.findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION).map { re -> re.text }.contains("it")
            val parameters = node.findChildByType(ElementType.FUNCTION_LITERAL)?.findChildByType(ElementType.VALUE_PARAMETER_LIST)
            if (parameters == null && isIt) {
                Warnings.TOO_MANY_LINES_IN_LAMBDA.warn(configRules, emitWarn, isFixMode,
                    "max length lambda without arguments is ${configuration.maxLambdaLength}, but you have $sizeLambda",
                    node.startOffset, node)
            }
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
    }
}
