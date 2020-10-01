package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_LONG_FUNCTION
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

/**
 * Rule 5.1.1 check function length
 */
class FunctionLength(private val configRules: List<RulesConfig>) : Rule("function-length") {

    companion object {
        val FUNCTION_ALLOW_COMMENT = listOf(EOL_COMMENT, KDOC, BLOCK_COMMENT)
        private const val MAX_FUNCTION_LENGTH = 30L
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    @Suppress("UnsafeCallOnNullableType")
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = FunctionLengthConfiguration(
                configRules.getRuleConfig(TOO_LONG_FUNCTION)?.configuration ?: mapOf()
        )

        if (node.elementType == FUN) {
            val copyNode = if (configuration.isIncludeHeader) node.copyElement() else (node.psi as KtFunction).bodyExpression?.node
            checkFun(copyNode, configuration.maxFunctionLength, node.startOffset)
        }
    }

    private fun checkFun(node: ASTNode?, maxFunctionLength: Long, startOffset: Int) {
        if (node == null) return
        node.findAllNodesWithCondition({ it.elementType in FUNCTION_ALLOW_COMMENT }).forEach { it.treeParent.removeChild(it) }
        val functionText = node.text.lines().filter { it.isNotBlank() }
        if (functionText.size > maxFunctionLength)
            TOO_LONG_FUNCTION.warn(configRules, emitWarn, isFixMode,
                    "max length is $maxFunctionLength, but you have ${functionText.size}", startOffset, node)
    }

    class FunctionLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxFunctionLength = config["maxFunctionLength"]?.toLong() ?: MAX_FUNCTION_LENGTH
        val isIncludeHeader = config["isIncludeHeader"]?.toBoolean() ?: true
    }
}
