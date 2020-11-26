package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FUN
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_LONG_FUNCTION
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

/**
 * Rule 5.1.1 check function length
 */
class FunctionLength(private val configRules: List<RulesConfig>) : Rule("function-length") {

    companion object {
        private const val MAX_FUNCTION_LENGTH = 30L
    }

    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = FunctionLengthConfiguration(
                configRules.getRuleConfig(TOO_LONG_FUNCTION)?.configuration ?: mapOf()
        )

        if (node.elementType == FUN) {
            checkFun(node, configuration)
        }
    }

    private fun checkFun(node: ASTNode, configuration: FunctionLengthConfiguration) {
        val copyNode = if (configuration.isIncludeHeader) {
            node.clone() as ASTNode
        } else {
            ((node.psi as KtFunction).bodyExpression?.node?.clone() ?: return) as ASTNode
        }
        copyNode.findAllNodesWithCondition({ it.elementType in COMMENT_TYPE }).forEach { it.treeParent.removeChild(it) }
        val functionText = copyNode.text.lines().filter { it.isNotBlank() }
        if (functionText.size > configuration.maxFunctionLength)
            TOO_LONG_FUNCTION.warn(configRules, emitWarn, isFixMode,
                    "max length is ${configuration.maxFunctionLength}, but you have ${functionText.size}",
                    node.startOffset, node)
    }

    class FunctionLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxFunctionLength = config["maxFunctionLength"]?.toLong() ?: MAX_FUNCTION_LENGTH
        val isIncludeHeader = config["isIncludeHeader"]?.toBoolean() ?: true
    }
}
